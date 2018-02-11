package ru.mhistory.providers;

import android.location.Location;
import android.util.Pair;

import java.util.Set;

import api.vo.Poi;
import api.vo.PoiInfo;
import ru.mhistory.geo.LatLng;
import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;
import ru.mhistory.provider.PoiSearchResult;

/**
 * Created by shcherbakov on 25.11.2017.
 */

public class PoiSearch {

    public static Pair<LatLng, LatLng> getSquare(LatLng coordinat, int distance) {
        double lat = coordinat.latitude;
        double lng = coordinat.longitude;
        double deltaX = Math.abs(lat - calcLat(coordinat, distance));
        double deltaY = Math.abs(lng - calcLng(coordinat, distance));
        LatLng lefBotom, rightTop;
        lefBotom = new LatLng(lat - deltaX, lng - deltaY);
        rightTop = new LatLng(lat + deltaX, lng + deltaY);
        return new Pair<>(lefBotom, rightTop);
    }


    private static double calcLat(LatLng coord, int dist) {
        double lat = Math.toRadians(coord.latitude);
        double lng = Math.toRadians(coord.longitude);
        double earthRadius = 6371000;
        return Math.toDegrees(lat - dist / earthRadius);
    }

    private static double calcLng(LatLng coord, int dist) {
        double lat = Math.toRadians(coord.latitude);
        double lng = Math.toRadians(coord.longitude);
        double earthRadius = 6371000;
        double a = (Math.cos(dist / earthRadius) - Math.pow(Math.sin(lat), 2)) / Math.pow(Math.cos(lat), 2);

        double c = Math.toDegrees(lng - Math.acos(a));
        return c;
    }

    public static PoiInfo getPoiInfo(LatLng current, LatLng remote) {
        float[] direction = new float[2];
        Location.distanceBetween(current.latitude, current.longitude,
                remote.latitude, remote.longitude, direction);

        return new PoiInfo(direction[0], direction[1]);
    }

    public static PoiInfo getPoiInfo(double latitude1, double longitude1, double latitude2, double longitude2) {
        float[] direction = new float[2];
        Location.distanceBetween(latitude1, longitude1,
                latitude2, longitude2, direction);

        return new PoiInfo(direction[0], direction[1]);
    }

    @Deprecated
    public static PoiSearchResult findPoi(LatLng current, Set<Poi> pois, int minRadius, int maxRadius) {
        PoiSearchResult poiResult = new PoiSearchResult();
        for (Poi poi : pois) {
            PoiInfo poiInfo = getPoiInfo(current.latitude, current.longitude, poi.latitude, poi.longitude);
            if (poiInfo.distanceTo <= minRadius) poiResult.withinMinRadius.put(poiInfo, poi);
            if (poiInfo.distanceTo > minRadius && poiInfo.distanceTo <= maxRadius)
                poiResult.betweenMinAndMaxRadius.put(poiInfo, poi);
        }
        return poiResult;
    }

    public static PoiSearchZoneResult findPoi(LatLng current, Set<Poi> pois, SearchConf conf) {
        PoiSearchZoneResult poiResult = new PoiSearchZoneResult();
        for (Poi poi : pois) {
            PoiInfo poiInfo = getPoiInfo(current.latitude, current.longitude, poi.latitude, poi.longitude);
            float poiFromMoveAngle = conf.movementAngle - poiInfo.angle;
           //Todo после выбора алгоритма оптимизмровать поиск
            if (poiInfo.distanceTo <= conf.radiusStay) {
                poiResult.stay.put(poiInfo, poi);
            }
            if (poiInfo.distanceTo <= conf.radiusZone3) {
                if (poiInfo.distanceTo <= conf.radiusZone1) poiResult.zone1.put(poiInfo, poi);
                else if (poiInfo.distanceTo <= conf.radiusZone2
                        && isRangeAngleMirror(conf.deltaAngleZona2, poiFromMoveAngle))
                    poiResult.zone2.put(poiInfo, poi);
                else if (!isRangeAngleMirror(conf.deltaAngleZona2, poiFromMoveAngle)
                        && isRangeAngleMirror(conf.deltaAngleZona3, poiFromMoveAngle))
                    poiResult.zone3.put(poiInfo, poi);
                else poiResult.zone0.put(poiInfo, poi);
            }
        }


        return poiResult;
    }

    private static boolean isRangeAngleMirror(float zoneAngle, float poiFromMoveAngle) { //лежит ли angle2  в секторе angle1

        return Math.abs(poiFromMoveAngle) <= zoneAngle;
    }

    @Override
    public String toString() {
        //todo  вывод точек по зонам
        return "PoiSearch{}";
    }
}
