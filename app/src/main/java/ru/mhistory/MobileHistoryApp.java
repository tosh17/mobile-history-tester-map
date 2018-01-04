package ru.mhistory;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.widget.Toast;

import ru.mhistory.common.util.FileUtil;
import ru.mhistory.log.FileLogger;
import ru.mhistory.log.Logger;

public class MobileHistoryApp extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        context = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        configureLogger();
        Toast.makeText(context, FileLogger.patchLogDir, Toast.LENGTH_LONG).show();
    }

    private void configureLogger() {
        Logger.setLoggingEnabled(BuildConfig.LOGGING);
         Logger.setLogToFile(BuildConfig.LOG_TO_FILE, this);
    }
}