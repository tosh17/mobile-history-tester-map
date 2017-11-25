package ru.mhistory.screen.map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;

import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import api.vo.Poi;
import api.vo.PoiContent;
import api.vo.PoiInfo;
import ru.mhistory.R;
import ru.mhistory.bus.BusProvider;
import ru.mhistory.bus.event.BDCompliteEvent;
import ru.mhistory.bus.event.CanPauseEvent;
import ru.mhistory.bus.event.CanPlayEvent;
import ru.mhistory.bus.event.LoadJsonFromServerEvent;
import ru.mhistory.bus.event.NextTrackInfoEvent;
import ru.mhistory.bus.event.PlaybackStopEvent;
import ru.mhistory.bus.event.PoiFoundEvent;
import ru.mhistory.bus.event.ResetTrackingEvent;
import ru.mhistory.bus.event.SetMaxPoiRadiusEvent;
import ru.mhistory.bus.event.SetStoryFileEvent;
import ru.mhistory.bus.event.StartTrackingEvent;
import ru.mhistory.bus.event.StopTrackingEvent;
import ru.mhistory.bus.event.TrackPlaybackEndedEvent;
import ru.mhistory.bus.event.TrackProgressEvent;
import ru.mhistory.common.util.PermissionUtils;
import ru.mhistory.common.util.ThreadUtil;
import ru.mhistory.common.util.TimeUtil;
import ru.mhistory.geo.GoogleApiLocationTracker;
import ru.mhistory.geo.LatLng;
import ru.mhistory.geo.LocationTracker;
import ru.mhistory.log.Logger;
import ru.mhistory.playback.AudioService;
import ru.mhistory.provider.DefaultPoiFinder;
import ru.mhistory.provider.FilePoiProvider;
import ru.mhistory.provider.PoiProviderConfig;
import ru.mhistory.provider.PoiSearchResult;
import ru.mhistory.providers.JsonToReal;
import ru.mhistory.providers.PoiSearch;
import ru.mhistory.providers.ServerFtpLoader;
import ru.mhistory.providers.ServerLoaderProvider;
import ru.mhistory.provider.UriFileProviderDelegate;
import ru.mhistory.realm.RealmFactory;
import ru.mhistory.screen.main.ui.DebugInfoFragment;
import ru.mhistory.screen.main.ui.widget.NumberPickerDialog;

public class MapPresenter implements LocationTracker.LocationUpdateCallbacks {
    private static final int MIN_LOCATION_UPDATE_INTERVAL_SEC = 1;
    private static final int MAX_LOCATION_UPDATE_INTERVAL_SEC = 300;
    private static final int MIN_POI_MIN_RADIUS_M = 100;
    private static final int MAX_POI_MIN_RADIUS_M = 5000;
    private static final int POI_MIN_RADIUS_STEP_M = 100;
    private static final int MIN_POI_MAX_RADIUS_M = 10_000;
    private static final int MAX_POI_MAX_RADIUS_M = 100_000;
    private static final int POI_MAX_RADIUS_STEP_M = 10_000;

    private static final String[] poiMinRadiusValues = buildPoiRadiusValues(
            MIN_POI_MIN_RADIUS_M, MAX_POI_MIN_RADIUS_M, POI_MIN_RADIUS_STEP_M);
    private static final String[] poiMaxRadiusValues = buildPoiRadiusValues(
            MIN_POI_MAX_RADIUS_M, MAX_POI_MAX_RADIUS_M, POI_MAX_RADIUS_STEP_M);

    private Set<Long> processedIdContent = new HashSet<>();  //прослушаные треки
    private Set<Poi> fullListPoi=new HashSet<>();
    private Set<Poi> processedPois = new HashSet<>();          // прослушаные POI
    private Set<Poi> latestPois = new HashSet<>();
    private final LocationTracker locationTracker;
    private LatLng currentLatLng;  // текущее положение
    private int currentAngel = 0;   //текущий угол
    private boolean isTracing = false;
    private final FilePoiProvider poiProvider;
    private final PoiProviderConfig poiProviderConfig;
    private DebugInfoFragment fragment;

    @NonNull
    private static String[] buildPoiRadiusValues(int min, int max, int step) {
        int count = max / step;
        String[] values = new String[count];
        for (int i = 0; i < count; i++) {
            values[i] = String.valueOf(min + i * step);
        }
        return values;
    }

    public MapPresenter(@NonNull Context context, @NonNull PermissionUtils.Requester requester) {
        this.locationTracker = new GoogleApiLocationTracker(context, requester);
        this.poiProvider = new FilePoiProvider();
        poiProviderConfig = new PoiProviderConfig();
        poiProvider.setProviderConfig(poiProviderConfig);
        locationTracker.setLocationUpdateCallbacks(this);
        locationTracker.setLocationUpdateIntervalMs(10 * 1000);

    }

    @UiThread
    public void attach(@NonNull DebugInfoFragment fragment) {
        this.fragment = fragment;
        BusProvider.getInstance().register(this);
        Logger.start();
    }

    @UiThread
    public void detach() {
        this.fragment = null;
        Logger.stop();
    }

    @UiThread
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        Context ctx = fragment.getActivity();
        ctx.stopService(new Intent(ctx, AudioService.class));
        locationTracker.setLocationUpdateCallbacks(null);
        locationTracker.setTrackingCallbacks(null);
        locationTracker.stopTracking();
        locationTracker.resetTracking();
    }

    @UiThread
    public void onLocationUpdateIntervalClicked() {
        NumberPickerDialog dialog = new NumberPickerDialog(
                fragment.getContext(),
                number -> {
                    Logger.d(String.format("Location update interval changed to (%s) sec", number));
                    locationTracker.setLocationUpdateIntervalMs(number * 1000);
                    fragment.setLocationUpdateIntervalSec(number);
                },
                locationTracker.getLocationUpdateIntervalMs() / 1000,
                MIN_LOCATION_UPDATE_INTERVAL_SEC,
                MAX_LOCATION_UPDATE_INTERVAL_SEC,
                R.string.update_interval_min
        );
        dialog.show();
    }

    @UiThread
    public void onPoiMinRadiusClicked() {
        NumberPickerDialog dialog = new NumberPickerDialog(
                fragment.getContext(),
                number -> {
                    int radius = number * POI_MIN_RADIUS_STEP_M;
                    Logger.d(String.format("Poi MIN radius changed to (%s) meters", radius));
                    poiProviderConfig.setMinRadiusInMeters(radius);
                    fragment.setPoiMinRadius(radius);
                },
                poiProviderConfig.getMinRadiusInMeters() / POI_MIN_RADIUS_STEP_M,
                1,
                MAX_POI_MIN_RADIUS_M / POI_MIN_RADIUS_STEP_M,
                R.string.choose_poi_min_radius
        );
        dialog.setDisplayedValues(poiMinRadiusValues);
        dialog.show();
    }

    @UiThread
    public void onPoiMaxRadiusClicked() {
        NumberPickerDialog dialog = new NumberPickerDialog(
                fragment.getContext(),
                number -> {
                    int radius = number * POI_MAX_RADIUS_STEP_M;
                    Logger.d(String.format("Poi MAX radius changed to (%s) meters", radius));
                    poiProviderConfig.setMaxRadiusInMeters(radius);
                    fragment.setPoiMaxRadius(radius);
                    BusProvider.getInstance().post(new SetMaxPoiRadiusEvent(radius));
                },
                poiProviderConfig.getMaxRadiusInMeters() / POI_MAX_RADIUS_STEP_M,
                1,
                MAX_POI_MAX_RADIUS_M / POI_MAX_RADIUS_STEP_M,
                R.string.choose_poi_max_radius
        );
        dialog.setDisplayedValues(poiMaxRadiusValues);
        dialog.show();
    }

    @WorkerThread
    @Override
    public void onLocationChanged(@NonNull final LatLng latLng) {
        if (!isTracing) currentLatLng = latLng;
        float[] result = new float[2];
        Location.distanceBetween(latLng.latitude, latLng.longitude,
                currentLatLng.latitude, currentLatLng.longitude, result);

        if (result[0] < 5 && isTracing) return;
        if(!isTracing){
            android.util.Pair<LatLng, LatLng> square = PoiSearch.getSquare(latLng, 100000);
            RealmFactory factory=RealmFactory.getInstance();
            fullListPoi=factory.findSquare(square);
            latestPois=fullListPoi;
        }
        isTracing = true;
        Logger.i("Map", "onLocationChanged " + "lat=" + latLng.latitude + ":lng" + latLng.longitude + " distance" + result[0] + "; angle" + result[1]);
        currentLatLng = latLng;
        PoiSearch.getSquare(currentLatLng,10000);
        //TODO: speed
        //https://stackoverflow.com/questions/15570542/determining-the-speed-of-a-vehicle-using-gps-in-android
        notifyUiOnLocationChanged(latLng);
        nextPoiFind(latLng);
    }

    private void nextPoiFind(LatLng latLng) {

       // PoiSearchResult pois = poiProvider.findPois(latLng, latestPois);
        PoiSearchResult pois =PoiSearch.findPoi(latLng,latestPois,10000,20000);
        if (pois != null && !pois.isEmpty()) {
            pois.removeAll(processedPois);
            latestPois = pois.getAllPoi();
            if (pois.isEmpty()) {
                Logger.d("Points exists within current radius but all of them are processed");
            } else {
                Pair<PoiInfo, Poi> resultPoi = pois.getNearestPoi();
                //noinspection ConstantConditions
                notifyUiOnNearestNonVisitedPoiAvailable(resultPoi.second, resultPoi.first);
            }
        }

    }

    private void notifyUiOnNearestNonVisitedPoiAvailable(@NonNull final Poi poi,
                                                         @NonNull final PoiInfo poiInfo) {
        ThreadUtil.runOnUiThread(() -> {
            updateUiWithPoi(poi, poiInfo);
            startPlayingAudioUrlForAvailablePoiIfPossible(poi);
        });
    }

    private void notifyUiOnLocationChanged(@NonNull final LatLng latLng) {
        ThreadUtil.runOnUiThread(() -> updateUiWithLocation(latLng.longitude, latLng.latitude));
    }

    //todo удалить после внедрения базы данных
    @Subscribe
    public void bdCompliteEvent(@NonNull BDCompliteEvent event) {
        Logger.i("MapPresent", "База подготовлена");
        startTracking();
    }

    @UiThread
    public void setStoryFileUri(@Nullable Uri uri) {
        Logger.d(String.format("Story file is chosen (%s)", uri));
        poiProvider.setDelegate(uri != null
                ? new UriFileProviderDelegate(uri, new DefaultPoiFinder())
                : null);
        BusProvider.getInstance().post(new SetStoryFileEvent());
    }

    @UiThread
    private void updateUiWithPoi(@NonNull Poi poi,
                                 @NonNull PoiInfo poiInfo) {
        Logger.d(String.format("New interest point is available (%s), distance to = %s, " +
                "angle = %s", poi, poiInfo.distanceTo, poiInfo.angle));
//        String compoundPoiDesc = String.format("%s\nlon=%s, lat=%s",
//                poi.desc, poi.longitude, poi.latitude);
//           fragment.setPoi(compoundPoiDesc);
        fragment.setPoi(poi.full_name);
        BusProvider.getInstance().post(new PoiFoundEvent(poi));
    }

    @UiThread
    private void updateUiWithLocation(double longitude, double latitude) {
        Logger.d(String.format("Location changed: lat = (%s), lon = (%s). " +
                "Poi is not available", latitude, longitude));
        fragment.setCoordinates(longitude, latitude);
    }

    @UiThread
    public void startTracking() {
        Logger.d("Start tracking locations...");
        locationTracker.startTracking();
        fragment.updateUiOnStartTracking();
        BusProvider.getInstance().post(new StartTrackingEvent());
    }

    @UiThread
    public void stopTracking() {
        Logger.d("Stop tracking locations...");
        locationTracker.stopTracking();
        fragment.updateUiOnStopTracking();
        BusProvider.getInstance().post(new StopTrackingEvent());
    }

    @UiThread
    public void resetTracking() {
        Logger.d("Reset tracking");
        processedIdContent.clear();
        processedPois.clear();
        locationTracker.resetTracking();
        fragment.updateUiOnResetTracking();
        BusProvider.getInstance().post(new ResetTrackingEvent());
    }

    @UiThread
    public int getLocationUpdateIntervalSec() {
        return locationTracker.getLocationUpdateIntervalMs() / 1000;
    }

    @UiThread
    public void playOrPauseTrack() {
        Context ctx = fragment.getActivity();
        ctx.startService(new Intent(ctx, AudioService.class)
                .setAction(AudioService.Action.PLAY_OR_PAUSE));
    }

    @UiThread
    public void playTrackToPosition(int position) {
        Context ctx = fragment.getActivity();
        ctx.startService(new Intent(ctx, AudioService.class)
                .setAction(AudioService.Action.PLAY_TO_POSITION).putExtra(AudioService.KEY_AUDIO_POSITION, position));
    }

    @UiThread
    public void playNextTrack() {
        Context ctx = fragment.getActivity();
        ctx.startService(new Intent(ctx, AudioService.class)
                .setAction(AudioService.Action.NEXT));
    }

    @UiThread
    public void stopPlay() {
        Context ctx = fragment.getActivity();
        ctx.startService(new Intent(ctx, AudioService.class)
                .setAction(AudioService.Action.STOP));
    }

    @Subscribe
    public void onNextTrackInfoEvent(@NonNull NextTrackInfoEvent event) {
        Logger.d("Audio track info received: name = %s, duration = %s", event.trackName,
                event.duration);
        locationTracker.stopTracking();
        fragment.setNextAudioTrackInfo(event.trackName, event.trackSequence, event.totalTrackCount);
    }

    @Subscribe
    public void onUpdateTrackProgressEvent(@NonNull TrackProgressEvent event) {
        fragment.updateAudioTrackDurations(TimeUtil.msToString(event.totalDurationMs),
                TimeUtil.msToString(event.currentDurationMs));
        fragment.updateAudioTrackDurationsSeekBar((int) (100 * event.currentDurationMs / event.totalDurationMs));
    }

    @Subscribe
    public void onCanPlayTrackEvent(@NonNull CanPlayEvent event) {
        locationTracker.startTracking();
        fragment.showPlayControl();
    }

    @Subscribe
    public void onCanPauseTrackEvent(@NonNull CanPauseEvent event) {
        fragment.showPauseControl();
    }

    @Subscribe
    public void onTrackPlaybackEndedEvent(@NonNull TrackPlaybackEndedEvent event) {
      //  Logger.d("Audio track playback completed, name = %s", event.trackName);
        Logger.i("Player", "ended -> " + event.trackName);
        nextPoiFind(currentLatLng);
        locationTracker.startTracking();
        fragment.hidePlayerControls();
    }

    @Subscribe
    public void onPlaybackStoppedEvent(@NonNull PlaybackStopEvent event) {
        fragment.clearAudioTrackInfo();
        fragment.hidePlayerControls();
    }

    private void startPlayingAudioUrlForAvailablePoiIfPossible(@NonNull Poi poi) {
        //todo переделать логику под wow
        if (poi.size() == 0) {
            Logger.d("There are no audio urls for poi (%s)", poi);
            processedPois.add(poi);
            return;
        }
        String audioUrlToPlay = null;
        PoiContent.AudioType audioTypeToPlay = null;
        while(poi.isHasNextContent()){
            //TODO: сделать выбор tts or mp3
            String audioUrl = poi.getNextContent().getDefaultUrl();
            Long idUrl=poi.getCurrentContent().id;
            PoiContent.AudioType audioType = poi.getCurrentContent().getDefaultType();
            if (!processedIdContent.contains(idUrl)) {
                audioUrlToPlay = audioUrl;
                audioTypeToPlay = audioType;
                //TODO: добовлять после того как трек проигран?
                processedIdContent.add(idUrl);
                break;
            }
        }
        if (audioUrlToPlay == null) {
            Logger.d("There are no available audio urls for poi (%s), all of them processed", poi);
            processedPois.add(poi);
            nextPoiFind(currentLatLng);
            return;
        }
        Context ctx = fragment.getActivity();
       // audioTypeToPlay= PoiContent.AudioType.MP3;
       // audioUrlToPlay="337034.wav";
        switch(audioTypeToPlay) {
            case MP3:
            ctx.startService(new Intent(ctx, AudioService.class)
                    .setAction(AudioService.Action.URL)
                    .putExtra(AudioService.KEY_AUDIO_URL, audioUrlToPlay));
        break;
            case TTS:
            ctx.startService(new Intent(ctx, AudioService.class)
                    .setAction(AudioService.Action.TEXT)
                    .putExtra(AudioService.KEY_AUDIO_URL, audioUrlToPlay));
            break;
        }
    }
}
