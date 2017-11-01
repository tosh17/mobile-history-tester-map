package ru.mhistory.geo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ru.mhistory.MobileHistoryApp;
import ru.mhistory.R;
import ru.mhistory.common.util.ThreadUtil;

public class DummyLocationTracker implements LocationTracker {
    private static final int MSG_NEXT_LOCATION = 0;
    private static final int DEFAULT_LOCATION_UPDATE_INTERVAL_SEC = 1;

    private int currentPosition = -1;
    private List<LatLng> cache;
    private HandlerThread workerThread;
    private Handler locationUpdateHandler;
    private TrackingCallbacks trackingCallbacks;
    private LocationUpdateCallbacks locationUpdateCallbacks;
    private volatile int locationUpdateIntervalMs = DEFAULT_LOCATION_UPDATE_INTERVAL_SEC * 1000;

    public void setTrackingCallbacks(@Nullable TrackingCallbacks trackingCallbacks) {
        this.trackingCallbacks = trackingCallbacks;
    }

    @Override
    public void setLocationUpdateCallbacks(@Nullable LocationUpdateCallbacks callbacks) {
        this.locationUpdateCallbacks = callbacks;
    }

    @Override
    public void setLocationUpdateIntervalMs(int intervalMs) {
        locationUpdateIntervalMs = intervalMs;
        if (locationUpdateHandler != null) {
            boolean hasMessages = locationUpdateHandler.hasMessages(MSG_NEXT_LOCATION);
            locationUpdateHandler.removeMessages(MSG_NEXT_LOCATION);
            if (hasMessages) {
                locationUpdateHandler.sendEmptyMessageDelayed(MSG_NEXT_LOCATION, intervalMs);
            }
        }
    }

    @Override
    public int getLocationUpdateIntervalMs() {
        return locationUpdateIntervalMs;
    }

    @Override
    public void startTracking() {
        if (workerThread == null) {
            workerThread = new HandlerThread("Worker-thread", Process.THREAD_PRIORITY_BACKGROUND);
            workerThread.start();
            locationUpdateHandler = new MyHandler(this, workerThread.getLooper());
        }
        locationUpdateHandler.sendEmptyMessageDelayed(MSG_NEXT_LOCATION, 0);
        if (trackingCallbacks != null) {
            trackingCallbacks.onStartTracking();
        }
    }

    @Override
    public void stopTracking() {
        if (locationUpdateHandler != null) {
            locationUpdateHandler.removeMessages(MSG_NEXT_LOCATION);
            locationUpdateHandler = null;
        }
        if (workerThread != null) {
            workerThread.quit();
            workerThread = null;
        }
        if (trackingCallbacks != null) {
            trackingCallbacks.onStopTracking();
        }
    }

    @Override
    public void resetTracking() {
        currentPosition = -1;
        if (trackingCallbacks != null) {
            trackingCallbacks.onResetTracking();
        }
    }

    @WorkerThread
    private void notifyGetNextLocation() {
        ensureCacheExists();
        currentPosition++;
        if (currentPosition >= cache.size()) {
            ThreadUtil.runOnUiThread(() -> {
                if (workerThread == null) {
                    return;
                }
                stopTracking();
                resetTracking();
            });
            return;
        }
        LatLng nextLatLng = getLocation();
        if (nextLatLng != null && locationUpdateCallbacks != null) {
            locationUpdateCallbacks.onLocationChanged(nextLatLng);
        }
        locationUpdateHandler.sendEmptyMessageDelayed(MSG_NEXT_LOCATION, locationUpdateIntervalMs);
    }

    @WorkerThread
    private void ensureCacheExists() {
        if (cache == null) {
            cache = readFromFile();
        }
    }

    @WorkerThread
    @Nullable
    private LatLng getLocation() {
        return cache.get(currentPosition);
    }

    @NonNull
    private List<LatLng> readFromFile() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(MobileHistoryApp.getContext()
                    .getResources().openRawResource(R.raw.route), "UTF-8"));
            return readLines(br);
        } catch (UnsupportedEncodingException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @NonNull
    private List<LatLng> readLines(BufferedReader br) throws IOException {
        List<LatLng> coordinates = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] coords = line.split(",");
            if (coords.length != 2) {
                continue;
            }
            coordinates.add(new LatLng(Double.parseDouble(coords[0]),
                    Double.parseDouble(coords[1])
            ));
        }
        return coordinates;
    }


    private static class MyHandler extends Handler {
        private final WeakReference<DummyLocationTracker> trackerRef;

        private MyHandler(@NonNull DummyLocationTracker tracker,
                          @NonNull Looper looper) {
            super(looper);
            trackerRef = new WeakReference<>(tracker);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEXT_LOCATION:
                    onNextLocationMessage();
                    break;
            }
        }

        private void onNextLocationMessage() {
            DummyLocationTracker tracker = trackerRef.get();
            if (tracker != null) {
                tracker.notifyGetNextLocation();
            }
        }
    }
}
