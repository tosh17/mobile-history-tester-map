package ru.mhistory.common.util;

import android.support.annotation.NonNull;

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
}
