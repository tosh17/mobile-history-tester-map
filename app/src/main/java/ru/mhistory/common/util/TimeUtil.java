package ru.mhistory.common.util;

import android.support.annotation.NonNull;

public class TimeUtil {

    @NonNull
    public static String msToString(long ms) {
        String finalTimerString = "";
        String secondsString;
        int hours = (int) (ms / (1000 * 60 * 60));
        int minutes = (int) (ms % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((ms % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }
        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        return finalTimerString + minutes + ":" + secondsString;
    }
    public static int stringToMs(String strTime) {
        String[] time=strTime.split(":");
        return Integer.parseInt(time[0])*60+Integer.parseInt(time[1]);
    }
    private TimeUtil() {
    }
}
