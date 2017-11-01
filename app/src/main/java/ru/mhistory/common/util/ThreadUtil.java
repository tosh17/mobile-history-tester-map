package ru.mhistory.common.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

public class ThreadUtil {
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(@NonNull Runnable runnable) {
        if (Looper.myLooper() != sHandler.getLooper()) {
            sHandler.post(runnable);
        } else {
            runnable.run();
        }
    }
}