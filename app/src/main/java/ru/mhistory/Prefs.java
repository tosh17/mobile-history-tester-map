package ru.mhistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Prefs {
    public static final String KEY_LAST_SEEN_VERSION = "last_seen_version";
    public static final String KEY_LATEST_CAMERA_POSITION = "latest_camera_position";

    private final SharedPreferences delegate;

    public Prefs(@NonNull Context context) {
        delegate = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void putString(@NonNull String key, @Nullable String value) {
        delegate.edit().putString(key, value).apply();
    }

    public String getString(@NonNull String key) {
        return delegate.getString(key, null);
    }

    public void putInt(@NonNull String key, int value) {
        delegate.edit().putInt(key, value).apply();
    }

    public int getInt(@NonNull String key, int defaultValue) {
        return delegate.getInt(key, defaultValue);
    }

    public void putBoolean(@NonNull String key, boolean value) {
        delegate.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return delegate.getBoolean(key, defaultValue);
    }

    public void remove(@NonNull String key) {
        delegate.edit().remove(key).apply();
    }
}