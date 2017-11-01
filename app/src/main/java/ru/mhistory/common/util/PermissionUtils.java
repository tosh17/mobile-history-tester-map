package ru.mhistory.common.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public abstract class PermissionUtils {
    @IntDef({PERMISSION_GRANTED, PERMISSION_DENIED})
    @Retention(RetentionPolicy.SOURCE)
    @interface PermissionResult {
    }

    @PermissionResult
    public static int checkAllSelfPermission(@NonNull Context context,
                                             @NonNull String... permissions) {
        if (permissions.length == 0) {
            throw new IllegalAccessError("No permissions specified");
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return PERMISSION_GRANTED;
        }
        for (String permission : permissions) {
            try {
                int result = ContextCompat.checkSelfPermission(context, permission);
                if (result != PERMISSION_GRANTED) {
                    return PERMISSION_DENIED;
                }
            } catch (Exception e) {
                // workaround for permissions check on Lenovo devices
                return PERMISSION_DENIED;
            }
        }
        return PERMISSION_GRANTED;
    }

    @PermissionResult
    public static int checkAnySelfPermission(@NonNull Context context,
                                             @NonNull String... permissions) {
        if (permissions.length == 0) {
            throw new IllegalAccessError("No permissions specified");
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return PERMISSION_GRANTED;
        }
        for (String permission : permissions) {
            try {
                int result = ContextCompat.checkSelfPermission(context, permission);
                if (result == PERMISSION_GRANTED) {
                    return PERMISSION_GRANTED;
                }
            } catch (Exception ignore) {
                // workaround for permissions check on Lenovo devices
                // continue with other permissions
            }
        }
        return PERMISSION_DENIED;
    }

    @PermissionResult
    public static int getGrantResult(@NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return PERMISSION_GRANTED;
        }
        for (int grantResult : grantResults) {
            if (grantResult != PERMISSION_GRANTED) {
                return PERMISSION_DENIED;
            }
        }
        return PERMISSION_GRANTED;
    }


    @NonNull
    public static Requester createRequester(@NonNull Fragment fragment, int requestCode) {
        return new FragmentRequester(fragment, requestCode);
    }

    public interface Requester {
        void requestPermissions(@NonNull String... permissions);
    }


    private static final class FragmentRequester implements Requester {
        private final Fragment fragment;
        private final int requestCode;

        public FragmentRequester(@NonNull Fragment fragment, int requestCode) {
            this.fragment = fragment;
            this.requestCode = requestCode;
        }

        @Override
        public void requestPermissions(@NonNull String... permissions) {
            fragment.requestPermissions(permissions, requestCode);
        }
    }
}
