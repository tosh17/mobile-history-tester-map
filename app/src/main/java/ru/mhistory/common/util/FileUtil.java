package ru.mhistory.common.util;

import android.support.annotation.NonNull;

import java.io.File;

import ru.mhistory.log.LogType;
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
