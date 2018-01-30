package ru.mhistory.fileservice;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.StringDef;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import ru.mhistory.bus.event.InitStatus;
import ru.mhistory.common.util.FileUtil;
import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;
import ru.mhistory.providers.JsonToReal;
import ru.mhistory.realm.RealmFactory;

import static ru.mhistory.fileservice.FileService.Action.*;

public class FileService extends Service {
    private boolean isDebug = false;
    @StringDef({
            LOAD_ALL_BD,
    })
    public @interface Action {
        String LOAD_ALL_BD = "ru.mhistory.fileservice.action.LOAD_ALL_BD";

    }

    public FileService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (isDebug) android.os.Debug.waitForDebugger();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Logger.d(LogType.Load, "Start fileService %s",action);
        switch(action){
            case LOAD_ALL_BD:
                InitStatus.send(InitStatus.FileLoadStart);
       //         loadBD("allpois");
                loadBD("34pois");
                break;
        }
        return START_NOT_STICKY;
    }

    private void loadBD(String name) {
        //todo заменить имена файлов
        String tempName = "cache/"+name+".zip";
        String jsonName = name+".json";
        File cachDir = new File(getApplicationContext().getFilesDir() + "/cache");
        if (!cachDir.exists()) {
            Log.d("MAKE DIR", cachDir.mkdir() + "");
        }
        FileUtil.listDir(getApplicationContext().getFilesDir().getAbsolutePath());

        //todo check free space
        //https://stackoverflow.com/questions/2941552/how-can-i-check-how-much-free-space-an-sd-card-mounted-on-an-android-device-has
        ServerLoaderProvider serverLoader = new ServerFtpLoader(name);
        File file = new File(getApplicationContext().getFilesDir() + "/" + tempName);
        serverLoader.load(file, new ServerLoaderProvider.onServerLoadFinish() {
            @Override
            public void loadFinished(boolean statusLoad) {
                try {
                    InitStatus.send(InitStatus.FileLoadStop);
                    serverLoader.unzip(getApplicationContext().getFilesDir().toString() + "/", tempName);
                    Uri uri = Uri.fromFile(new File(getApplicationContext().getFilesDir() + "/" + jsonName));
                    RealmFactory.getInstance(getApplicationContext());
                    new JsonToReal(uri).doIt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
