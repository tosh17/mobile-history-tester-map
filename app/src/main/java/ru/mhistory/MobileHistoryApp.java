package ru.mhistory;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.yandex.metrica.YandexMetrica;

import ru.mhistory.log.Logger;

public class MobileHistoryApp extends Application {

    private static Context context;
    private String API_key="06581a8f-8122-4510-98f1-2603e7ac0408";

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
        YandexMetrica.activate(getApplicationContext(), API_key);
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(this);
       // Toast.makeText(context, FileLogger.patchLogDir, Toast.LENGTH_LONG).show();

        YandexMetrica.reportEvent("Srart Aplication");
    }

    private void configureLogger() {
        Logger.setLoggingEnabled(BuildConfig.LOGGING);
         Logger.setLogToFile(BuildConfig.LOG_TO_FILE, this);
    }
}