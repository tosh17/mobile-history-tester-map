package ru.mhistory.common.util;

import android.support.annotation.IntDef;

import ru.mhistory.BuildConfig;

public class AppLaunchChecker {
    @IntDef({FIRST_TIME, FIRST_TIME_VERSION, REGULAR})
    @interface Code {
    }

    public static final int FIRST_TIME = 0;
    public static final int FIRST_TIME_VERSION = 1;
    public static final int REGULAR = 2;

    @Code
    public static int checkAppLaunch(int lastSeenVersionCode) {
        return checkAppLaunch(BuildConfig.VERSION_CODE, lastSeenVersionCode);
    }

    @Code
    private static int checkAppLaunch(int currentVersionCode, int lastSeenVersionCode) {
        if (lastSeenVersionCode == -1) {
            return FIRST_TIME;
        } else if (lastSeenVersionCode < currentVersionCode) {
            return FIRST_TIME_VERSION;
        } else if (lastSeenVersionCode >= currentVersionCode) {
            return REGULAR;
        } else {
            return REGULAR;
        }
    }

    private AppLaunchChecker() {
        // empty
    }
}
