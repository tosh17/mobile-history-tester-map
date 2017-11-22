package ru.mhistory.common.util;

import android.support.annotation.NonNull;

import java.io.File;

import ru.mhistory.log.Logger;

public class FileUtil {
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
        //String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";
        Logger.i("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Logger.i("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Logger.i("Files", "FileName:" + files[i].getName());
        }
    }
}
