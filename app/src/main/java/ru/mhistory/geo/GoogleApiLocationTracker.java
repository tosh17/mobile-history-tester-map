package ru.mhistory.geo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ru.mhistory.common.util.PermissionUtils;
import ru.mhistory.log.Logger;

public class GoogleApiLocationTracker implements LocationTracker {
    private static final int DEFAULT_LOCATION_UPDATE_INTERVAL_SEC = 3;

    private final Context appContext;
    private HandlerThread workerThread;
    private GoogleApiClient googleApiClient;
    private PermissionUtils.Requester permissionRequester;
    private LocationRequest locationRequest;
    private boolean requestingLocationUpdates = false;
    private LocationUpdateCallbacks locationUpdateCallbacks;
    private volatile int locationUpdateIntervalMs = DEFAULT_LOCATION_UPDATE_INTERVAL_SEC * 1000;

    @SuppressWarnings("FieldCanBeLocal")
    private final GoogleApiClient.ConnectionCallbacks innerConnectionCallbacks
            = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            onGoogleApiClientConnected(bundle);
        }

        @Override
        public void onConnectionSuspended(int cause) {
            onGoogleApiClientConnectionSuspended(cause);
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final GoogleApiClient.OnConnectionFailedListener innerConnectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    onGoogleApiClientConnectionFailed(connectionResult);
                }
            };

    private final LocationListener innerLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (locationUpdateCallbacks != null) {
                Logger.d("New location available: %s", location);
                 locationUpdateCallbacks.onLocationChanged(new LatLng(location.getLatitude(),
                        location.getLongitude()),location.getTime());
            }
        }
    };

    public GoogleApiLocationTracker(@NonNull Context context,
                                    @NonNull PermissionUtils.Requester requester) {
        appContext = context.getApplicationContext();
        permissionRequester = requester;
        createGoogleApiClient();
        createLocationRequest();
    }

    private void createGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(appContext)
                .addConnectionCallbacks(innerConnectionCallbacks)
                .addOnConnectionFailedListener(innerConnectionFailedListener)
                .addApi(LocationServices.API)
                .build();
    }

    private void createLocationRequest() {
        long updateIntervalMs = locationUpdateIntervalMs;
        locationRequest = new LocationRequest()
                .setInterval(updateIntervalMs)
                .setFastestInterval(updateIntervalMs / 2)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void startTracking() {
        Logger.d("START TRACKING");
        requestingLocationUpdates = true;
        startWorkerThread();
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        } else {
            startLocationUpdates();
        }
    }

    private void startWorkerThread() {
        if (workerThread == null) {
            workerThread = new HandlerThread("Worker-thread", Process.THREAD_PRIORITY_DEFAULT);
            workerThread.start();
        }
    }

    @Override
    public void stopTracking() {
        Logger.d("STOP TRACKING");
        requestingLocationUpdates = false;
        stopLocationUpdates();
        stopWorkerThread();
    }

    private void stopWorkerThread() {
        if (workerThread != null) {
            workerThread.quit();
            workerThread = null;
        }
    }

    @Override
    public void resetTracking() {
        Logger.d("RESET TRACKING");
        googleApiClient.disconnect();
    }

    @Override
    public void setLocationUpdateIntervalMs(int intervalMs) {
        locationUpdateIntervalMs = intervalMs;
        stopLocationUpdates();
        createLocationRequest();
        startLocationUpdates();
    }

    @Override
    public int getLocationUpdateIntervalMs() {
        return locationUpdateIntervalMs;
    }

    @Override
    public void setTrackingCallbacks(@Nullable TrackingCallbacks callbacks) {
    }

    @Override
    public void setLocationUpdateCallbacks(@Nullable LocationUpdateCallbacks callbacks) {
        locationUpdateCallbacks = callbacks;
    }

    private void onGoogleApiClientConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    private void onGoogleApiClientConnectionSuspended(int cause) {
        Logger.w("Google api client connection suspended with cause = " + cause);
        googleApiClient.connect();
    }

    private void onGoogleApiClientConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logger.w("Google api client connection failed, result = " + connectionResult);
    }

    private void startLocationUpdates() {
        Logger.d("Starting location updates...");
        if (PermissionUtils.checkAnySelfPermission(appContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (requestingLocationUpdates) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                        locationRequest, innerLocationListener, workerThread.getLooper());
            } else {
                Logger.d("Requested for location updates but location tracking is not started");
            }
        } else {
            permissionRequester.requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void stopLocationUpdates() {
        Logger.d("Stopping location updates...");
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,
                    innerLocationListener);
        }
    }
}
