package ru.mhistory.common.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.io.File;


import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;

public class FileUtil {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    @NonNull
    public static String getNameFromPath(@NonNull String path) {
        int indexOfLastSlash = path.lastIndexOf('/');
        return indexOfLastSlash > -1
                ? path.substring(indexOfLastSlash + 1)
                : path;
    }

    private FileUtil() {
    }
    public static void listDir(String path){
        Logger.d(LogType.File, "Path: %s",path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Logger.d(LogType.File, "Size: %d",files.length);
        for (int i = 0; i < files.length; i++)
        {
            Logger.d(LogType.File, "FileName: %s",files[i].getName());
        }
    }
}
