package ru.mhistory;

import android.app.Application;
import android.content.Context;

import ru.mhistory.log.Logger;

public class MobileHistoryApp extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        configureLogger();
    }

    private void configureLogger() {
        Logger.setLoggingEnabled(BuildConfig.LOGGING);
        Logger.setLogToFile(BuildConfig.LOG_TO_FILE, this);
    }
}