package ru.mhistory.geo;

import com.google.android.gms.location.LocationRequest;

public class LocationRequestDefaults {
    private static final long REQUEST_INTERVAL_HIGH_MS = 10 * 1000;
    private static final long REQUEST_INTERVAL_MS = 5000;
    private static final long REQUEST_FASTEST_INTERVAL_HIGH_MS = 30 * 1000;
    private static final long REQUEST_FASTEST_INTERVAL_MS = 5 * 1000;

    public static LocationRequest get(@LocationAccuracy int accuracy) {
        LocationRequest request = LocationRequest.create();
        switch (accuracy) {
            case LocationAccuracy.HIGH:
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(REQUEST_INTERVAL_HIGH_MS)
                        .setFastestInterval(REQUEST_FASTEST_INTERVAL_HIGH_MS);
                break;
            case LocationAccuracy.MEDIUM:
                request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(REQUEST_INTERVAL_MS)
                        .setFastestInterval(REQUEST_FASTEST_INTERVAL_MS);
                break;
            case LocationAccuracy.LOW:
                request.setPriority(LocationRequest.PRIORITY_LOW_POWER)
                        .setInterval(REQUEST_INTERVAL_MS)
                        .setFastestInterval(REQUEST_FASTEST_INTERVAL_MS);
                break;
            case LocationAccuracy.LOWEST:
                request.setPriority(LocationRequest.PRIORITY_NO_POWER)
                        .setInterval(REQUEST_INTERVAL_MS)
                        .setFastestInterval(REQUEST_FASTEST_INTERVAL_MS);
                break;
            default:
                request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(REQUEST_INTERVAL_MS)
                        .setFastestInterval(REQUEST_FASTEST_INTERVAL_MS);
                break;
        }
        return request;
    }

    private LocationRequestDefaults() {
        // empty
    }
}
