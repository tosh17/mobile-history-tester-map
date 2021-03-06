package ru.mhistory.screen.map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import api.vo.AngleAvgLatLng;
import api.vo.Poi;
import api.vo.PoiContent;
import api.vo.PoiInfo;
import ru.mhistory.bus.BusProvider;
import ru.mhistory.bus.event.AppPrepareBDCompleteEvent;
import ru.mhistory.bus.event.AppPrepareMapReadyCompleteEvent;
import ru.mhistory.bus.event.AppPrepareTTSCompleteEvent;
import ru.mhistory.bus.event.CanPauseEvent;
import ru.mhistory.bus.event.CanPlayEvent;
import ru.mhistory.bus.event.InitStatus;
import ru.mhistory.bus.event.LocationChange;
import ru.mhistory.bus.event.MapButtonClick;
import ru.mhistory.bus.event.MapButtonState;
import ru.mhistory.bus.event.NextTrackInfoEvent;
import ru.mhistory.bus.event.PlaybackStopEvent;
import ru.mhistory.bus.event.PoiCacheAvailableEvent;
import ru.mhistory.bus.event.PoiFoundEvent;
import ru.mhistory.bus.event.PoiStatusChangeEvent;
import ru.mhistory.bus.event.ResetTrackingEvent;
import ru.mhistory.bus.event.SetMaxPoiRadiusEvent;
import ru.mhistory.bus.event.StartTrackingEvent;
import ru.mhistory.bus.event.StopTrackingEvent;
import ru.mhistory.bus.event.TrackPlaybackEndedEvent;
import ru.mhistory.bus.event.TrackProgressEvent;
import ru.mhistory.common.util.PermissionUtils;
import ru.mhistory.common.util.ThreadUtil;
import ru.mhistory.common.util.TimeUtil;
import ru.mhistory.geo.DummyLocationTracker;
import ru.mhistory.geo.GoogleApiLocationTracker;
import ru.mhistory.geo.LatLng;
import ru.mhistory.geo.LocationTracker;
import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;
import ru.mhistory.playback.AudioService;
import ru.mhistory.playback.Preambula;
import ru.mhistory.providers.PoiSearch;
import ru.mhistory.providers.PoiSearchZoneResult;
import ru.mhistory.providers.SearchConf;
import ru.mhistory.realm.RealmFactory;
import ru.mhistory.screen.main.ui.PlayerMenuFragment;

public class MapPresenter implements LocationTracker.LocationUpdateCallbacks {

    private Set<Poi> fullListPoi = new HashSet<>(); //полный лист
    private Set<Poi> playingListPoi = new HashSet<>(); //лист точек которые хоть раз играли
    private Set<Poi> listingListPoi = new HashSet<>(); //лист точек при переборе пои
    private Pair<PoiInfo, Poi> lastListingPoi;
    private Pair<PoiInfo, Poi> currentPlayingPoi;
    private String lastListingPoiUrl = "";
    private String currentPlayingPoiUrl = "";
    private CopyOnWriteArraySet<Poi> listPoi = new CopyOnWriteArraySet<>();

    private final LocationTracker locationTracker;
    private LatLng currentLatLng;  // текущее положение
    private LatLng FirstLatLng;
    private long lastTimeStamp = 0; // последняя временная метка
    private SearchConf conf;
    private boolean isTracing = false; //поигрешность на скачки трекера
    private boolean isStay = true;      //стоим или движемся
    private boolean isPlayerBlock = false;      //плайер блокирован
    private boolean isStateTracing = false;
    private AngleAvgLatLng avgangle;
    private long preambulaId; //


    private boolean startTrackingIsBdComplite = false;
    private boolean startTrackingIsTTSDone = false;
    private boolean startTrackingIsMapDone = false;

    //temp
    public double maxDistance = 0;
    String tempStr = "";
    private PlayerMenuFragment fragment;

    public MapPresenter(@NonNull Context context, @NonNull PermissionUtils.Requester requester) {
        this.locationTracker = new GoogleApiLocationTracker(context, requester);
        //this.locationTracker = new DummyLocationTracker();
        locationTracker.setLocationUpdateCallbacks(this);
        conf = SearchConf.getSearchPoiConf(context);
        locationTracker.setLocationUpdateIntervalMs(conf.searchTimeUpdate * 1000);
        avgangle = new AngleAvgLatLng(conf.angleAvgCount);

        conf.setChangeListener(new SearchConf.OnChangeSearchPoiConf() {
            @Override
            public void onChangeSearchPoiConf() {
                locationTracker.setLocationUpdateIntervalMs(conf.searchTimeUpdate * 1000);
                if (isStay) setSearchingRadius(conf.radiusStay);
                else setSearchingRadius(conf.radiusZone3);
                if (isStay && conf.isStayPlay) nextPoiFind(currentLatLng);
                fragment.debugShow(conf.debug);
                if (conf.angleAvgCount != avgangle.size())
                    avgangle = new AngleAvgLatLng(conf.angleAvgCount);
            }
        });

        locationTracker.startTracking();
    }

    @UiThread
    public void attach(@NonNull PlayerMenuFragment fragment) {
        this.fragment = fragment;
        fragment.isDebug = conf.debug;
        BusProvider.getInstance().register(this);

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

    @WorkerThread
    @Override
    public void onLocationChanged(@NonNull final LatLng latLng, long time) {
        ThreadUtil.runOnUiThread(() ->
                BusProvider.getInstance().post(new LocationChange(latLng, conf.movementAngle)));

        if (fullListPoi != null) marker(latLng);
        if (!isTracing) {
            currentLatLng = latLng;
            setSearchingRadius(conf.radiusStay);
        }
        float[] result = new float[2];
        Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude,
                latLng.latitude, latLng.longitude,
                result);
        float speed = 1000 * result[0] / (time - lastTimeStamp);
        if (speed > conf.angleAvgSpeed)
            conf.movementAngle = avgangle.add(latLng);
        currentLatLng = latLng;
        lastTimeStamp = time;
        if (!isStateTracing) return;
        if (isPlayerBlock) return;
        if (isStay) {  //Вывод в шторку максимального перемещения для режима стоим
            maxDistance = Math.max(result[0], maxDistance);
            ThreadUtil.runOnUiThread(() -> fragment.setTest(tempStr));
        }
        if (!isTracing) {//todo добавить загрузку нового квадрата  и разбить по квадратам
            firstTracking(latLng);
        }
        if (isStay) {
            float[] tempResult = new float[2];
            Location.distanceBetween(FirstLatLng.latitude, FirstLatLng.longitude,
                    latLng.latitude, latLng.longitude,
                    tempResult);
            if (tempResult[0] < conf.deltaDistanceToTracking) {
                if (conf.isStayPlay) nextPoiFind(latLng);
                return;
            } else {
                isStay = false;
                setSearchingRadius(conf.radiusZone3);
                Logger.d(LogType.Location, "Начало движения");
            }
        }
        Logger.d(LogType.Location, "onLocationChanged " + "lat=" + latLng.latitude + ":lng" + latLng.longitude
                + " distance" + result[0] + "; angle=" + result[1]);


        notifyUiOnLocationChanged(latLng);
        nextPoiFind(latLng);
    }

    private void firstTracking(LatLng latLng) {
        FirstLatLng=latLng;
        InitStatus.send(InitStatus.BdLoadStart);
        android.util.Pair<LatLng, LatLng> square = PoiSearch.getSquare(latLng, conf.searchSquare);
        RealmFactory factory = RealmFactory.getInstance(fragment.getActivity().getApplicationContext());
        fullListPoi = factory.findSquare(square);
        for (Poi p : fullListPoi) if (!p.isPoiComplete()) listPoi.add(p);
        Map<LatLng, Poi> cache = new HashMap<>();
        InitStatus.send(InitStatus.BdLoadStop);
        for (Poi p : fullListPoi) cache.put(new LatLng(p.latitude, p.longitude), p);
        ThreadUtil.runOnUiThread(() ->
                BusProvider.getInstance().post(new PoiCacheAvailableEvent(cache)));
        isTracing = true;
        notifyUiOnLocationChanged(latLng);
        setSearchingRadius(conf.radiusStay);
    }

    private void marker(LatLng latLng) {
        int radius = isStay ? conf.radiusStay : conf.radiusZone3;
        float[] direction = new float[2];
        for (Poi p : fullListPoi) {
            if (currentPlayingPoi == null || p != currentPlayingPoi.second) {
                Location.distanceBetween(latLng.latitude, latLng.longitude, p.latitude, p.longitude, direction);
                if (direction[0] > radius) {
                    if (p.status != 0) changePoiStatus(p, 0);
                } else {
                    changePoiStatus(p);
                }
            }
        }
    }

    private void nextPoiFind(LatLng latLng) {
        if (isStay && !conf.isStayPlay) return;
        if (isPlayerBlock) return;
        PoiSearchZoneResult pois = PoiSearch.findPoi(latLng, listPoi, conf);
        if (pois != null && !pois.isEmpty()) {
            ThreadUtil.runOnUiThread(() -> {
                if (isStay) {
                    tempStr = pois.stayToString();
                    fragment.setTest(pois.stayToString());
                } else {
                    tempStr = pois.moveToString(conf.movementAngle);
                    fragment.setTest(pois.moveToString(conf.movementAngle));
                }
            });
            if (pois.isEmpty(isStay)) {
                Logger.d("Points exists within current radius but all of them are processed");
            } else {
                Pair<PoiInfo, Poi> resultPoi;
                resultPoi = pois.getNearestPoi(isStay);

                //noinspection ConstantConditions
                notifyUiOnNearestNonVisitedPoiAvailable(resultPoi.second, resultPoi.first);
                currentPlayingPoi = resultPoi;
            }
        }
    }


    private Pair<PoiInfo, Poi> nextPoiFind() {
        PoiSearchZoneResult pois = PoiSearch.findPoi(currentLatLng, listPoi, conf);
        Pair<PoiInfo, Poi> resultPoi;
        resultPoi = pois.getNextNearestPoi(isStay, playingListPoi);
        if (resultPoi == null) resultPoi = pois.getNextNearestPoi(isStay, listingListPoi);
        if (resultPoi != null) {
            listingListPoi.add(resultPoi.second);
        }
        return resultPoi;
    }

    private void notifyUiOnNearestNonVisitedPoiAvailable(@NonNull final Poi poi,
                                                         @NonNull final PoiInfo poiInfo) {
        ThreadUtil.runOnUiThread(() -> {
            updateUiWithPoi(poi, poiInfo);
            startPlayingAudioUrlForAvailablePoiIfPossible(poi, poiInfo);
        });
    }

    private void notifyUiOnLocationChanged(@NonNull final LatLng latLng) {
        ThreadUtil.runOnUiThread(() -> updateUiWithLocation(latLng.longitude, latLng.latitude));
    }

    private void setSearchingRadius(@NonNull int radius) {
        ThreadUtil.runOnUiThread(() -> BusProvider.getInstance().post(new SetMaxPoiRadiusEvent(radius)));
    }


    @Subscribe
    public void bdCompleteEvent(@NonNull AppPrepareBDCompleteEvent event) {
        Logger.d(LogType.App, "MapPresenter Base Complete ok");
        startTrackingIsBdComplite = true;
        startTracking();
    }

    @Subscribe
    public void ttsCompleteEvent(@NonNull AppPrepareTTSCompleteEvent event) {
        Logger.d(LogType.App, "MapPresenter TTS Complete ok");
        startTrackingIsTTSDone = true;
        startTracking();
    }

    @Subscribe
    public void mapReadyCompleteEvent(@NonNull AppPrepareMapReadyCompleteEvent event) {
        Logger.d(LogType.App, "MapPresenter MapReady Complete ok");
        startTrackingIsMapDone = true;
        startTracking();
    }

    @UiThread
    private void updateUiWithPoi(@NonNull Poi poi,
                                 @NonNull PoiInfo poiInfo) {
        Logger.d(String.format("New interest point is available (%s), distance to = %s, " +
                "angle = %s", poi, poiInfo.distanceTo, poiInfo.angle));
        fragment.setPoi(poi.full_name, poi.size(), poiInfo.distanceTo);
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
        if (!startTrackingIsBdComplite || !startTrackingIsTTSDone || !startTrackingIsMapDone)
            return;
        Logger.d("Start tracking locations...");
        Logger.d(LogType.Tester, "Start");
        isStateTracing = true;
        InitStatus.send(InitStatus.Finish);
        // locationTracker.startTracking();
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
//        processedIdContent.clear();
//        processedPois.clear();
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
        if (currentPlayingPoi == null) return;
        currentPlayingPoi.second.setFlip(true);
        String url = currentPlayingPoi.second.getNextFlipContent().getDefaultUrl();
        checkPrev(currentPlayingPoi.second.isHasPrevFlipContent());
        checkNext(currentPlayingPoi.second.isHasNextFlipContent());
        Context ctx = fragment.getActivity();
        ctx.startService(new Intent(ctx, AudioService.class)
                .setAction(AudioService.Action.NEXT).putExtra(AudioService.KEY_AUDIO_URL, url));

    }


    @UiThread
    public void playPrevTrack() {
        currentPlayingPoi.second.setFlip(true);
        String url = currentPlayingPoi.second.getPrevFlipContent().getDefaultUrl();
        checkPrev(currentPlayingPoi.second.isHasPrevFlipContent());
        checkNext(currentPlayingPoi.second.isHasNextFlipContent());
        Context ctx = fragment.getActivity();
        ctx.startService(new Intent(ctx, AudioService.class)
                .setAction(AudioService.Action.NEXT).putExtra(AudioService.KEY_AUDIO_URL, url));
    }

    public void playNextPoi() {
        Pair<PoiInfo, Poi> pois = nextPoiFind();
        if (pois == null) {
            Toast.makeText(fragment.getContext(), "В данной зоне нет точек", Toast.LENGTH_SHORT);
            return;
        }
        if (currentPlayingPoi.second != null) {
            changePoiStatus(currentPlayingPoi.second, currentPlayingPoi.second.poiActiveStatus());
            currentPlayingPoi.second.status = currentPlayingPoi.second.poiActiveStatus();
        }
        if (pois != null) {
            lastListingPoiUrl = currentPlayingPoiUrl;
            currentPlayingPoiUrl = pois.second.getNextContent().getDefaultUrl();
            lastListingPoi = currentPlayingPoi;
            currentPlayingPoi = pois;
            checkPrev(currentPlayingPoi.second.isHasPrev());
            checkNext(currentPlayingPoi.second.isHasNext());
            ;
            playingListPoi.add(currentPlayingPoi.second);
            ThreadUtil.runOnUiThread(() -> updateUiWithPoi(pois.second, pois.first));
            Context ctx = fragment.getActivity();
            ctx.startService(new Intent(ctx, AudioService.class)
                    .setAction(AudioService.Action.NEXT).putExtra(AudioService.KEY_AUDIO_URL, currentPlayingPoiUrl));
        }
    }

    public void playPrevPoi() {
        checkPrev(lastListingPoi.second.isHasNext());
        checkNext(lastListingPoi.second.isHasPrev());
        ThreadUtil.runOnUiThread(() -> updateUiWithPoi(lastListingPoi.second, lastListingPoi.first));
        Context ctx = fragment.getActivity();
        ctx.startService(new Intent(ctx, AudioService.class)
                .setAction(AudioService.Action.NEXT).putExtra(AudioService.KEY_AUDIO_URL, lastListingPoiUrl));
    }

    @Subscribe
    public void onMapButtonClick(@NonNull MapButtonClick event) {
        switch (event.type) {
            case MapButtonClick.buttonPausePlay:
                playOrPauseTrack();
                break;
            case MapButtonClick.buttonNextTrack:
                playNextTrack();
                break;
            case MapButtonClick.buttonNextPoi:
                playNextPoi();
                break;
        }
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
        Logger.d(LogType.Tester, "Stop");
        isStateTracing = false;
        //locationTracker.stopTracking();
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
        Logger.d(LogType.Tester, "Start");
        isStateTracing = true;
        locationTracker.startTracking();
        fragment.showPlayControl();
    }

    @Subscribe
    public void onCanPauseTrackEvent(@NonNull CanPauseEvent event) {
        fragment.showPauseControl();
    }

    @Subscribe
    public void onTrackPlaybackEndedEvent(@NonNull TrackPlaybackEndedEvent event) {
        Logger.d("Audio track playback completed, name = %b", event.startTracking);
        isPlayerBlock = false;
        currentPlayingPoi.second.putContentToHistory(event.id, fragment.getContext());
        if (currentPlayingPoi.second.isPoiComplete()) {
            changePoiStatus(currentPlayingPoi.second);
            listPoi.remove(currentPlayingPoi.second);
        }
        //nextPoiFind(currentLatLng);
        if (event.startTracking) {
            Logger.d(LogType.Tester, "Start");
            isStateTracing = true;
            //    locationTracker.startTracking();
        }

    }

    @Subscribe
    public void onPlaybackStoppedEvent(@NonNull PlaybackStopEvent event) {
        fragment.clearAudioTrackInfo();
        fragment.hidePlayerControls();
    }

    private void startPlayingAudioUrlForAvailablePoiIfPossible(@NonNull Poi poi, @NonNull PoiInfo poiInfo) {

        if (lastListingPoi == currentPlayingPoi) lastListingPoi = currentPlayingPoi;
        poi.setFlip(false);
        listingListPoi.clear();
        playingListPoi.add(poi);
        if (poi.isPoiComplete()) {
            Logger.d("There are no audio urls for poi or complite(%s)", poi);
            changePoiStatus(poi, 3);
            return;
        }
        isPlayerBlock = true;
        String audioUrlToPlay = null;
        long idUrlToPlay = 0;
        PoiContent.AudioType audioTypeToPlay = null;

        audioUrlToPlay = poi.getNextContent().getDefaultUrl();
        checkNext(poi.isHasNext());
        checkPrev(poi.isHasPrev());
        idUrlToPlay = poi.getCurrentContent().id;
        audioTypeToPlay = poi.getCurrentContent().getDefaultType();
        lastListingPoiUrl = currentPlayingPoiUrl;
        currentPlayingPoiUrl = audioUrlToPlay;
        Context ctx = fragment.getActivity();

        boolean isPreabpula = false;
        String preambula = null;
        if (!isStay && poi.objId != preambulaId) {
            preambulaId = poi.objId;
            isPreabpula = true;
            preambula = Preambula.get(poiInfo.distanceTo, conf.movementAngle - poiInfo.angle, conf) + ", " + poi.name;
        }


        switch (audioTypeToPlay) {
            case MP3:
                ctx.startService(new Intent(ctx, AudioService.class)
                        .setAction(AudioService.Action.URL)
                        .putExtra(AudioService.KEY_AUDIO_ID, idUrlToPlay)
                        .putExtra(AudioService.KEY_AUDIO_URL, audioUrlToPlay)
                        .putExtra(AudioService.KEY_IS_PREAMBULA, isPreabpula)
                        .putExtra(AudioService.KEY_PREAMBULA, preambula));
                break;
            case TTS:
                ctx.startService(new Intent(ctx, AudioService.class)
                        .setAction(AudioService.Action.TEXT)
                        .putExtra(AudioService.KEY_AUDIO_ID, idUrlToPlay)
                        .putExtra(AudioService.KEY_AUDIO_URL, audioUrlToPlay)
                        .putExtra(AudioService.KEY_IS_PREAMBULA, isPreabpula)
                        .putExtra(AudioService.KEY_PREAMBULA, preambula));
                break;
        }
    }

    private void checkNext(boolean isEnable) {
        fragment.nextTrackClickEnabled(isEnable);
        ThreadUtil.runOnUiThread(() -> BusProvider.getInstance().post(new MapButtonState(MapButtonState.BTN_NEXT_TRACK, isEnable)));

    }

    private void checkPrev(boolean isEnable) {
        fragment.prevTrackClickEnabled(isEnable);
    }

    private void changePoiStatus(Poi poi, int i) {
        poi.status = i;
        ThreadUtil.runOnUiThread(() -> BusProvider.getInstance().post(new PoiStatusChangeEvent(poi)));
    }

    private void changePoiStatus(Poi poi) {
        if (poi.status != poi.poiActiveStatus()) {
            changePoiStatus(poi, poi.poiActiveStatus());
            poi.status = poi.poiActiveStatus();
        }
    }


    public void clearHistory() {
        playingListPoi.clear();
        listingListPoi.clear();
        listPoi.clear();
        PoiSearchZoneResult pois = PoiSearch.findPoi(currentLatLng, listPoi, conf);
        for (Poi p : fullListPoi) {
            p.clearHistory();
            listPoi.add(p);
            if (pois.contains(p, isStay)) {
                changePoiStatus(p);
            } else {

                changePoiStatus(p, 0);
            }
        }
    }
}
