package ru.mhistory.providers;

import android.content.Context;

import ru.mhistory.Prefs;

/**
 * Created by shcherbakov on 26.11.2017.
 */

public class SearchConf {
    public int searchTimeUpdate=1;
    //todo настраевоемое или расчетное?
    public boolean isStayPlay = true;
    public int searchSquare = 1000000;
    public int reSearchSquare = 50000;

    public int deltaDistanceToTracking = 100;
    public int radiusStay = 5000;
    public int radiusZone3 = 10000;
    public int speedToMove = 1;
    public int radiusZone1 = 500;
    public float movementAngle = 0;
    public float deltaAngleZona2 = 45;
    public int radiusZone2 = 2000;
    public float deltaAngleZona3 = 120;

    private static SearchConf conf = null;
    private OnChangeSearchPoiConf onChangeSearchPoiCon;

    public interface OnChangeSearchPoiConf {
        public void onChangeSearchPoiConf();
    }

    private SearchConf() {
    }

    public static SearchConf getSearchPoiConf(Context context) {
        if (conf == null) {
            conf = new SearchConf();
            conf.load(context);
        }
        return conf;
    }

    public void load(Context context) {
        Prefs pref = new Prefs(context);
        searchTimeUpdate= pref.getInt(Prefs.KEY_POI_SEARCH_TIME_UPDATE, searchTimeUpdate);
        isStayPlay = pref.getBoolean(Prefs.KEY_POI_SEARCH_IS_STAY_PLAY, isStayPlay);
        deltaDistanceToTracking = pref.getInt(Prefs.KEY_POI_SEARCH_deltaDistanceToTracking, deltaDistanceToTracking);
        radiusStay = pref.getInt(Prefs.KEY_POI_SEARCH_radiusStay, radiusStay);
        radiusZone3 = pref.getInt(Prefs.KEY_POI_SEARCH_radiusMove, radiusZone3);
        radiusZone1 = pref.getInt(Prefs.KEY_POI_SEARCH_radiusZone1, radiusZone1);
        deltaAngleZona2 = pref.getFloat(Prefs.KEY_POI_SEARCH_deltaAngleZona2, deltaAngleZona2);
        radiusZone2 = pref.getInt(Prefs.KEY_POI_SEARCH_radiusZone2, radiusZone2);
        deltaAngleZona3 = pref.getFloat(Prefs.KEY_POI_SEARCH_deltaAngleZona3, deltaAngleZona3);


    }

    public void save(Context context) {
        Prefs pref = new Prefs(context);
        pref.putInt(Prefs.KEY_POI_SEARCH_TIME_UPDATE, searchTimeUpdate);
        pref.putBoolean(Prefs.KEY_POI_SEARCH_IS_STAY_PLAY, isStayPlay);
        pref.putInt(Prefs.KEY_POI_SEARCH_deltaDistanceToTracking, deltaDistanceToTracking);
        pref.putInt(Prefs.KEY_POI_SEARCH_radiusStay, radiusStay);
        pref.putInt(Prefs.KEY_POI_SEARCH_radiusMove, radiusZone3);
        pref.putInt(Prefs.KEY_POI_SEARCH_radiusZone1, radiusZone1);
        pref.putFloat(Prefs.KEY_POI_SEARCH_deltaAngleZona2, deltaAngleZona2);
        pref.putInt(Prefs.KEY_POI_SEARCH_radiusZone2, radiusZone2);
        pref.putFloat(Prefs.KEY_POI_SEARCH_deltaAngleZona3, deltaAngleZona3);
        update();
    }

    public void setChangeListener(OnChangeSearchPoiConf onChangeSearchPoiConf) {
        this.onChangeSearchPoiCon = onChangeSearchPoiConf;
    }

    public void update() {
        onChangeSearchPoiCon.onChangeSearchPoiConf();
    }
}
