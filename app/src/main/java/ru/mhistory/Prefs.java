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

    //config PoiSearch
    public static final String KEY_POI_SEARCH_TIME_UPDATE ="poi_search_searchTimeUpdate" ;//= 1;
    public static final String KEY_POI_SEARCH_IS_STAY_PLAY ="poi_search_isStayPlay" ;//= true;
    public static final String KEY_POI_SEARCH_searchSquare="poi_search_searchSquare" ;//= 100000;
    public static final String KEY_POI_SEARCH_reSearchSquare ="poi_search_reSearchSquare" ;//= 50000;
    public static final String KEY_POI_SEARCH_deltaDistanceToTracking="poi_search_deltaDistanceToTracking" ;// = 100;
    public static final String KEY_POI_SEARCH_radiusStay ="poi_search_radiusStay" ;//= 5000;
    public static final String KEY_POI_SEARCH_radiusMove ="poi_search_radiusMove" ;//= 10000;
    public static final String KEY_POI_SEARCH_radiusZone1 ="poi_search_radiusZone1" ;//= 500;
    public static final String KEY_POI_SEARCH_deltaAngleZona2 ="poi_search_deltaAngleZona2" ;//= 45;
    public static final String KEY_POI_SEARCH_radiusZone2 ="poi_search_radiusZone2" ;//2000;
    public static final String KEY_POI_SEARCH_deltaAngleZona3 ="poi_search_deltaAngleZona3" ;//= 120;


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

    public float getFloat(@NonNull String key, float defaultValue) {
        return delegate.getFloat(key, defaultValue);
    }

    public void putFloat(@NonNull String key, float value) {
        delegate.edit().putFloat(key, value).apply();

    }
}