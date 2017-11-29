package ru.mhistory.geo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

public interface LocationTracker {
    interface TrackingCallbacks {
        void onStartTracking();

        void onStopTracking();

        void onResetTracking();
    }


    interface LocationUpdateCallbacks {
        void onLocationChanged(@NonNull LatLng latLng, long time);
    }


    @UiThread
    void setTrackingCallbacks(@Nullable TrackingCallbacks callbacks);

    @UiThread
    void setLocationUpdateCallbacks(@Nullable LocationUpdateCallbacks callbacks);

    @UiThread
    void startTracking();

    @UiThread
    void stopTracking();

    @UiThread
    void resetTracking();

    @UiThread
    void setLocationUpdateIntervalMs(int intervalMs);

    int getLocationUpdateIntervalMs();
}