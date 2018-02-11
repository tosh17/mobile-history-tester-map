package ru.mhistory.screen.map;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dmstocking.optional.java.util.Optional;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;
import com.yandex.metrica.YandexMetrica;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mhistory.BuildConfig;
import ru.mhistory.Prefs;
import ru.mhistory.R;
import ru.mhistory.bus.BusProvider;
import ru.mhistory.bus.event.AppPrepareMapReadyCompleteEvent;
import ru.mhistory.bus.event.CanPauseEvent;
import ru.mhistory.bus.event.CanPlayEvent;
import ru.mhistory.bus.event.InitStatus;
import ru.mhistory.bus.event.LocationChange;
import ru.mhistory.bus.event.MapButtonClick;
import ru.mhistory.bus.event.MapButtonState;
import ru.mhistory.bus.event.NextTrackInfoEvent;
import ru.mhistory.bus.event.PoiCacheAvailableEvent;
import ru.mhistory.bus.event.PoiFoundEvent;
import ru.mhistory.bus.event.PoiReleasedEvent;
import ru.mhistory.bus.event.PoiStatusChangeEvent;
import ru.mhistory.bus.event.ResetTrackingEvent;
import ru.mhistory.bus.event.SetMaxPoiRadiusEvent;
import ru.mhistory.bus.event.SetStoryFileEvent;
import ru.mhistory.bus.event.StartTrackingEvent;
import ru.mhistory.bus.event.StopTrackingEvent;
import ru.mhistory.bus.event.TrackPlaybackEndedEvent;
import ru.mhistory.common.util.AppLaunchChecker;
import ru.mhistory.common.util.PermissionUtils;
import ru.mhistory.common.util.ThreadUtil;
import ru.mhistory.common.util.UiUtil;
import ru.mhistory.geo.LocationAccuracy;
import ru.mhistory.geo.LocationRequestDefaults;
import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;
import ru.mhistory.provider.PoiProviderConfig;
import ru.mhistory.providers.PoiSearch;
import ru.mhistory.screen.DrawerActivity;
import ru.mhistory.screen.map.ui.MhMapView;

public class MapActivity
        extends DrawerActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
        , LocationListener {
    private static final int REQUEST_LOCATION_PERMISSION = 0;
    private static final int REQUEST_RESOLVE_PLAY_SERVICES_ERROR = 1;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 2;
    private static final String KEY_MAP_VIEW_STATE = "mapViewState";
    private static final String KEY_RESOLVING_PLAY_SERVICES_ERROR = "resolvingPlayServicesError";
    private static final String KEY_REQUESTING_LOCATION_PERMISSION = "requestingLocationPermission";

    @BindView(R.id.map)
    MhMapView mapView;

    @BindView(R.id.ic_commpass)
    ImageView icCompass;

    @BindView(R.id.mapImageViewPausePlay)
    ImageView imageViewPausePlay;
    boolean isPlay = false;
    @BindDrawable(R.drawable.ic_player_controll_pause_button)
    Drawable pauseDrawable;
    @BindDrawable(R.drawable.ic_player_controll_play_button)
    Drawable playDrawable;

    @BindView(R.id.mapIcButtonNextTrack)
    ImageView icButtonNextTrack;
    @BindView(R.id.mapButtonNextTrack)
    View buttonNextTrack;

    @BindView(R.id.mapInfoCard)
    View mapInfoCard;
    @BindView(R.id.mapTextInfo)
    TextView mapTextInfo;
    @BindView(R.id.mapInfoProgress)
    ProgressBar mapInfoProgress;

    private Prefs prefs;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private Optional<LatLng> lastLatLng;
    private LocationRequest locationRequestHighAccuracy;
    private boolean resolvingPlayServicesError;
    private boolean requestingLocationPermission;
    private boolean locationSettingsGoodEnough;
    private boolean firstTimeLaunch;
    private boolean showSearchingRadius = true;
    private boolean showPoi = true;
    private boolean isCompass = true;
    private float currentBearing = 0;
    private android.widget.Toast toast;
    private PowerManager.WakeLock wl;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initSupportActionBar();
        prefs = new Prefs(this);
        ButterKnife.bind(this);
        firstTimeLaunch = checkAppLaunch();
        resolvingPlayServicesError = savedInstanceState != null
                && savedInstanceState.getBoolean(KEY_RESOLVING_PLAY_SERVICES_ERROR, false);
        requestingLocationPermission = savedInstanceState != null
                && savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_PERMISSION, false);
        mapView.onCreate(savedInstanceState != null
                ? savedInstanceState.getBundle(KEY_MAP_VIEW_STATE) : null);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::onMapReady);
//        mapPresenter = new MapPresenter();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //todo locationRequestHighAccuracy
        locationRequestHighAccuracy = LocationRequestDefaults.get(LocationAccuracy.MEDIUM);
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNjfdhotDimScreen");
        toInitService();
    }

    private void toInitService() {
        //  FileUtil.verifyStoragePermissions(this);
        Logger.start();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        BusProvider.getInstance().register(this);
//        mapPresenter.onAttachUi(this);
        checkLocationPermission(firstTimeLaunch, () -> googleApiClient.connect());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        wl.acquire();
        if (isLocationPermissionGranted() && locationSettingsGoodEnough) {
            startLocationUpdatesIfConnected();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        wl.release();
        //todo Listener
        //  stopLocationUpdatesIfConnected();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        BusProvider.getInstance().unregister(this);
//        mapPresenter.onDetachUi();
        googleApiClient.disconnect();
        Optional.ofNullable(googleMap).map(GoogleMap::getCameraPosition)
                .ifPresent(cameraPosition ->
                        prefs.putString(Prefs.KEY_LATEST_CAMERA_POSITION,
                                new Gson().toJson(cameraPosition)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void NvIsOpen(boolean stat) {
        mapView.setEnabled(!stat);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // FIXME: see https://goo.gl/T0to6L
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Bundle mapViewSaveState = new Bundle(outState);
        mapView.onSaveInstanceState(mapViewSaveState);
        outState.putBundle(KEY_MAP_VIEW_STATE, mapViewSaveState);
        mapView.onSaveInstanceState(outState);
        outState.putBoolean(KEY_RESOLVING_PLAY_SERVICES_ERROR, resolvingPlayServicesError);
        outState.putBoolean(KEY_REQUESTING_LOCATION_PERMISSION, requestingLocationPermission);
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_RESOLVE_PLAY_SERVICES_ERROR:
                resolvingPlayServicesError = false;
                if (resultCode == RESULT_OK) {
                    googleApiClient.connect();
                }
                break;
            case REQUEST_CHECK_LOCATION_SETTINGS:
                if (resultCode == RESULT_OK) {
                    locationSettingsGoodEnough = true;
                    mapView.setMyLocationEnabled(true);
                    startLocationUpdatesIfConnected();
                } else {
                    mapView.setMyLocationEnabled(false);
                }
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            requestingLocationPermission = false;
            if (PermissionUtils.getGrantResult(grantResults) == PackageManager.PERMISSION_GRANTED) {
                googleApiClient.connect();
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (lastLocation == null) {
            getLastKnownLocation().ifPresent(location -> {
                updateMyLocation(location);
                cameraToCenter();
            });
        }
        checkLocationSettings(firstTimeLaunch);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The GoogleAPIClient will re-connect automatically. No need to call its #connect.
//        Log.i(MapPresenter.class.getSimpleName(), "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (resolvingPlayServicesError) {
            return;
        }
        if (result.hasResolution()) {
            try {
                resolvingPlayServicesError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_PLAY_SERVICES_ERROR);
            } catch (IntentSender.SendIntentException ex) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
            }
        } else {
            showPlayServicesErrorDialog(result.getErrorCode());
            resolvingPlayServicesError = true;
        }
    }

    private void initSupportActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
    }

    private void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        cameraToLatestPosition();
        googleMap.setPadding(0, UiUtil.dpToPx(48), 0, 0);
        mapView.setShowPoi(showPoi);
        mapView.setShowSearchingRadius(showSearchingRadius);
        mapView.setSearchingRadius(new PoiProviderConfig().getMaxRadiusInMeters());
        ThreadUtil.runOnUiThread(() -> BusProvider.getInstance().post(new AppPrepareMapReadyCompleteEvent()));
    }

    private boolean checkAppLaunch() {
        int lastSeenVersion = prefs.getInt(Prefs.KEY_LAST_SEEN_VERSION, -1);
        int appLaunch = AppLaunchChecker.checkAppLaunch(lastSeenVersion);
        switch (appLaunch) {
            case AppLaunchChecker.FIRST_TIME:
                //Todo Сделать диалог с переходом на страницу занрузки
            case AppLaunchChecker.FIRST_TIME_VERSION:
                prefs.putInt(Prefs.KEY_LAST_SEEN_VERSION, BuildConfig.VERSION_CODE);
                break;
            case AppLaunchChecker.REGULAR:
                //TODO: check update
                break;
        }
        return appLaunch == AppLaunchChecker.FIRST_TIME;
    }

    private Optional<Location> getLastKnownLocation() {
        try {
            return Optional.ofNullable(LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient));
        } catch (SecurityException ex) {
            Log.w(MapActivity.class.getSimpleName(), "No location permissions granted");
            return Optional.empty();
        }
    }

    /**
     * Checks {@link Manifest.permission#ACCESS_FINE_LOCATION} permission at runtime.
     * Must be called from {@link Activity#onStart()} only.
     *
     * @param forceRequest If we need to make a permission request.
     */
    private void checkLocationPermission(boolean forceRequest, @NonNull Runnable ifGranted) {
        if (isLocationPermissionGranted()) {
            ifGranted.run();
        } else {
            if (forceRequest && !requestingLocationPermission) {
                requestLocationPermission();
            }
        }
    }

    private boolean isLocationPermissionGranted() {
        return PermissionUtils.checkAllSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestingLocationPermission = true;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    private void checkLocationSettings(boolean forceResolution) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequestHighAccuracy)
                .setAlwaysShow(true);
        LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build())
                .setResultCallback(locationSettingsResultCallback(forceResolution));
    }

    @SuppressWarnings("MissingPermission")
    private ResultCallback<LocationSettingsResult> locationSettingsResultCallback(
            boolean forceResolution) {
        return result -> {
            Status status = result.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    locationSettingsGoodEnough = true;
                    mapView.setMyLocationEnabled(true);
                    startLocationUpdatesIfConnected();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    if (forceResolution) {
                        try {
                            status.startResolutionForResult(MapActivity.this,
                                    REQUEST_CHECK_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException ignore) {
                        }
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }
        };
    }

    private void startLocationUpdatesIfConnected() {
        if (!googleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                    locationRequestHighAccuracy, this);
        } catch (SecurityException ex) {
            // TODO:
        }
    }

    private void stopLocationUpdatesIfConnected() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private void cameraToCenter() {
        if (googleMap == null) return;
        float currentZoom = googleMap.getCameraPosition().zoom;
        lastLatLng.ifPresent(latLng -> googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, currentZoom), 250, null));
    }

    private void cameraToBearing(float bearing) {
        float currentZoom = googleMap.getCameraPosition().zoom;

        lastLatLng.ifPresent(latLng -> {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)      // Sets the center of the map to Mountain View
                    .zoom(currentZoom)                   // Sets the zoom
                    .bearing(bearing)                // Sets the orientation of the camera to east
                    //  .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });
    }

    private void cameraToCenterMeters(int meters) {
        //    float currentZoom = googleMap.getCameraPosition().zoom;

//        lastLatLng.ifPresent(latLng -> {
//            Pair<ru.mhistory.geo.LatLng, ru.mhistory.geo.LatLng>
//                    square = PoiSearch.getSquare(
//                    new ru.mhistory.geo.LatLng(lastLatLng.get()), meters);
//            LatLngBounds SQUARE = new LatLngBounds(
//                    square.first.toGoogle(), square.second.toGoogle());
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SQUARE, 20));
//        });

//        lastLatLng.ifPresent(latLng -> {
//
//            Pair<ru.mhistory.geo.LatLng, ru.mhistory.geo.LatLng>
//                    square = PoiSearch.getSquare(
//                    new ru.mhistory.geo.LatLng(lastLatLng.get()), meters);
//            LatLngBounds SQUARE = new LatLngBounds(
//                    square.first.toGoogle(), square.second.toGoogle());
//            // Creates a CameraPosition from the builder
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(SQUARE,0));
//        });

        Pair<ru.mhistory.geo.LatLng, ru.mhistory.geo.LatLng>
                square = PoiSearch.getSquare(mapView.myLocation, meters);
        LatLngBounds SQUARE = new LatLngBounds(
                square.first.toGoogle(), square.second.toGoogle());
        // Creates a CameraPosition from the builder
      //  googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(SQUARE, 0));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(SQUARE, 0));
    }

    private void cameraToLatLng(@NonNull LatLng latLng) {
        float currentZoom = googleMap.getCameraPosition().zoom;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentZoom), 250, null);
    }

    private void cameraToLatestPosition() {
        String str = prefs.getString(Prefs.KEY_LATEST_CAMERA_POSITION);
        Optional.ofNullable(new Gson().fromJson(
                str, CameraPosition.class))
                .ifPresent(latestCameraPosition -> {

                    googleMap.moveCamera(
                            CameraUpdateFactory.newCameraPosition(latestCameraPosition));

                });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
   //     Logger.d(LogType.Location, "Map   " + location.toString());
        // updateMyLocation(location);
        // mapView.setMyLocation(location);
        //   cameraToLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void updateMyLocation(@NonNull Location location) {
        lastLocation = location;
        lastLatLng = Optional.of(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void updateMyLocation(@NonNull ru.mhistory.geo.LatLng location) {
        lastLatLng = Optional.of(new LatLng(location.latitude, location.longitude));
    }

    private void showPlayServicesErrorDialog(int errorCode) {
        PlayServicesErrorDialogFragment.newInstance(errorCode)
                .show(getSupportFragmentManager(), "PlayServicesErrorDialog");
    }

    private void onPlayServicesErrorDialogDismissed() {
        resolvingPlayServicesError = false;
    }


    @OnClick(R.id.iv_show_seaching_cicrle)
    public void onShowSearchingRadiusClicked() {
        showSearchingRadius = !showSearchingRadius;
        Toast.makeText(this, String.format("%s searching radius",
                showSearchingRadius ? "Show" : "Hide"), Toast.LENGTH_SHORT).show();
        mapView.setShowSearchingRadius(showSearchingRadius);
    }

    @OnClick(R.id.iv_show_poi)
    public void onShowPoiClicked() {
        showPoi = !showPoi;
        Toast.makeText(this, String.format("%s available poi",
                showPoi ? "Show" : "Hide"), Toast.LENGTH_SHORT).show();
        mapView.setShowPoi(showPoi);
    }

    @OnClick(R.id.btn_target_in_center)
    public void onTargetInCenterClicked() {
        checkLocationPermission(true, () -> {
            if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            } else {
                if (lastLocation != null) {
                    cameraToCenter();
                } else {
                    getLastKnownLocation().ifPresent(location -> {
                        updateMyLocation(location);
                        cameraToCenter();
                    });
                }
                checkLocationSettings(true);
            }
        });
    }

    @OnClick(R.id.btn_compass)
    public void onbtnCompass() {
        checkLocationPermission(true, () -> {
            if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            } else {
                isCompass = !isCompass;
                float bearing;
                if (isCompass) {
                    printToast("Режим Слежения Включен");
                    icCompass.setRotation(0);
                    bearing = currentBearing;
                } else {
                    printToast("Режим Слежения Выключен");
                    icCompass.setRotation(-45);
                    bearing = 0;
                }
                if (lastLocation != null) {

                    cameraToBearing(bearing);
                } else {
                    getLastKnownLocation().ifPresent(location -> {
                        updateMyLocation(location);
                        cameraToBearing(bearing);
                    });
                }
                checkLocationSettings(true);
            }
        });
    }

    @OnClick(R.id.btn_map_scale)
    public void onBtnMapScale() {
        checkLocationPermission(true, () -> {
            if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            } else {
                if (lastLocation != null) {
                    cameraToCenterMeters(15000);
                } else {
                    getLastKnownLocation().ifPresent(location -> {
                        updateMyLocation(location);
                        cameraToCenterMeters(15000);
                    });
                }
                checkLocationSettings(true);
            }
        });
    }

    @OnClick(R.id.mapButtonPausePlay)
    public void onbtnPausePlay() {
        BusProvider.getInstance().post(new MapButtonClick(MapButtonClick.buttonPausePlay));
        String eventParameters = "{\"name\":\"PlayPause\", \"location\":\"map\"}";
        YandexMetrica.reportEvent("Button press", eventParameters);
    }

    @OnClick(R.id.mapButtonNextTrack)
    public void onBtnNextTrack() {
        BusProvider.getInstance().post(new MapButtonClick(MapButtonClick.buttonNextTrack));
    }

    private void setButtonNextTrackEnable(boolean enable) {
        buttonNextTrack.setEnabled(enable);
        if (!enable)
            icButtonNextTrack.setColorFilter(getResources().getColor(R.color.mp_player_disable_btn), PorterDuff.Mode.SRC_ATOP);
        else
            icButtonNextTrack.setColorFilter(getResources().getColor(R.color.mp_player_enable_btn), PorterDuff.Mode.SRC_ATOP);
    }

    @OnClick(R.id.mapButtonNextPoi)
    public void onBtnNextPoi() {
        BusProvider.getInstance().post(new MapButtonClick(MapButtonClick.buttonNextPoi));
    }

    @Subscribe
    public void setButtonState(@NonNull MapButtonState event) {
        setButtonNextTrackEnable(event.stare);
    }

    @Subscribe
    public void infoStatus(@NonNull InitStatus event) {
        if (event.isInit) {
            mapInfoCard.setVisibility(View.GONE);
            return;
        }
        mapTextInfo.setText(getString(event.resId));
        mapInfoProgress.setProgress(event.progress);
    }

    @Subscribe
    public void onCanPlayTrackEvent(@NonNull CanPlayEvent event) {
        ThreadUtil.runOnUiThread(() -> imageViewPausePlay.setImageDrawable(playDrawable));
    }

    @Subscribe
    public void onCanPauseTrackEvent(@NonNull CanPauseEvent event) {
        ThreadUtil.runOnUiThread(() -> imageViewPausePlay.setImageDrawable(pauseDrawable));
    }

    @Subscribe
    public void onLocationChangeEvent(@NonNull LocationChange event) {
//        float currentZoom = googleMap.getCameraPosition().zoom;
//        googleMap.animateCamera(
//                CameraUpdateFactory.newLatLngZoom(event.location, currentZoom), 250, null));
        currentBearing = event.angle;
        if (!isCompass) {
            mapView.setMyLocation(event.location, 0);
            cameraToBearing(event.angle);
        } else mapView.setMyLocation(event.location, event.angle);
        updateMyLocation(event.location);
        cameraToCenter();
    }

    @Subscribe
    public void onPoiCacheAvailableEvent(@NonNull PoiCacheAvailableEvent event) {
        Logger.d("Poi cache available event received");
        mapView.setPoi(event.pois);
        mapView.setShowPoi(showPoi);
    }

    @Subscribe
    public void onStartTrackingEvent(@NonNull StartTrackingEvent event) {
        Logger.d("Start tracking event received");
    }

    @Subscribe
    public void onStopTrackingEvent(@NonNull StopTrackingEvent event) {
        Logger.d("Stop tracking event received");
    }

    @Subscribe
    public void onResetTrackingEvent(@NonNull ResetTrackingEvent event) {
        Logger.d("Reset tracking event received");
        mapView.onPoiReleased();
        mapView.setShowPoi(showPoi);
    }

    @Subscribe
    public void onClearStoryFileEvent(@NonNull SetStoryFileEvent event) {
        Logger.d("Clear story file event received");
        mapView.clearPoi();
    }

    @Subscribe
    public void onPoiFoundEvent(@NonNull PoiFoundEvent event) {
        Logger.d("Poi found event received");
        mapView.onPoiFound(event.poi);
    }

    @Subscribe
    public void onPoiStatusChangeEvent(@NonNull PoiStatusChangeEvent event) {
        Logger.d(LogType.Location,event.poi.name+ "change status to "+ event.poi.status);
        mapView.onPoiChange(event.poi);
    }

    @Subscribe
    public void onPoiReleasedEvent(@NonNull PoiReleasedEvent event) {
        Logger.d("Poi released event received");
        mapView.onPoiReleased();
    }

    @Subscribe
    public void onNextTrackInfoEvent(@NonNull NextTrackInfoEvent event) {
        Logger.d(String.format("Next track info event received %s", event.trackName));
        mapView.nextPoiTrackInfo();
    }

    @Subscribe
    public void onSetPoiRadiusEvent(@NonNull SetMaxPoiRadiusEvent event) {
        Logger.d(String.format("Set poi radius event received, radius = (%s)", event.radiusMeters));
        mapView.setSearchingRadius(event.radiusMeters);
        cameraToCenterMeters((int) (1.5*event.radiusMeters));
        // googleMap.animateCamera(CameraUpdateFactory.zoomTo(getZoomLevel(event.radiusMeters*4/3)), 2000, null);
        // googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
    }

    @Subscribe
    public void onTrackPlaybackEndedEvent(@NonNull TrackPlaybackEndedEvent event) {
        mapView.onPoiReleased();
    }

    public void printToast(String str) {
        if (toast == null) {
            toast = new Toast(this).makeText(this, str, Toast.LENGTH_SHORT);
        } else toast.setText(str);
        toast.show();
    }

    public static class PlayServicesErrorDialogFragment extends DialogFragment {
        private static final String ARG_PLAY_SERVICES_ERROR = "playServicesError";

        public static PlayServicesErrorDialogFragment newInstance(int errorCode) {
            PlayServicesErrorDialogFragment f = new PlayServicesErrorDialogFragment();
            Bundle args = new Bundle(1);
            args.putInt(ARG_PLAY_SERVICES_ERROR, errorCode);
            f.setArguments(args);
            return f;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int errorCode = this.getArguments().getInt(ARG_PLAY_SERVICES_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    getActivity(), errorCode, REQUEST_RESOLVE_PLAY_SERVICES_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MapActivity) getActivity()).onPlayServicesErrorDialogDismissed();
        }
    }
}
