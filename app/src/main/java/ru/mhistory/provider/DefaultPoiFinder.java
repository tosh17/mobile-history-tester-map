package ru.mhistory.provider;

import android.location.Location;
import android.support.annotation.NonNull;

import java.util.Map;
import java.util.Set;

import api.vo.Poi;
import api.vo.PoiInfo;
import ru.mhistory.geo.LatLng;

public class DefaultPoiFinder extends PoiFinder {
    private LatLng prevLatLng;
    private final float[] direction = new float[2];
    private final float[] result = new float[2];

    @NonNull
    @Override
    protected PoiSearchResult findPois(@NonNull LatLng latLng,
                                       long minRadiusInMeters,
                                       long maxRadiusInMeters,
                                       float angle,
                                       @NonNull Set<Poi> inputPois) {
        float motionBearing = 0f;
        // update current data based on the previous one: lat/lng, bearing
        if (prevLatLng != null) {
            Location.distanceBetween(prevLatLng.latitude, prevLatLng.longitude,
                    latLng.latitude, latLng.longitude, direction);
            motionBearing = direction[1];
            prevLatLng = latLng;
        }
        PoiSearchResult searchResult = new PoiSearchResult();
        findPois(latLng, minRadiusInMeters, maxRadiusInMeters, motionBearing, inputPois,
                searchResult);
        return searchResult;
    }

    private void findPois(@NonNull LatLng latLng,
                          long minRadiusInMeters,
                          long maxRadiusInMeters,
                          float motionBearing,
                          @NonNull Set<Poi> inputPois,
                          @NonNull PoiSearchResult searchResult) {
        for (Map.Entry<LatLng, Poi> nextEntry : cache.entrySet()) {
            Poi poi = nextEntry.getValue();
            LatLng poiLatLng = nextEntry.getKey();
            boolean hadPoi = inputPois.contains(poi);
            addPoiToSearchResultIfNecessary(latLng, minRadiusInMeters, maxRadiusInMeters,
                    motionBearing, poi, poiLatLng, hadPoi, searchResult);
            if (hadPoi) {
                inputPois.remove(poi);
            }
        }
        removePoiOutOfMaxRadiusIfNecessary(searchResult);
    }

    private void removePoiOutOfMaxRadiusIfNecessary(@NonNull PoiSearchResult searchResult) {
        boolean hasPoiWithinMaxRadius = searchResult.withinMinRadius.size() > 0
                || searchResult.betweenMinAndMaxRadius.size() > 0;
        if (hasPoiWithinMaxRadius) {
            searchResult.outOfMaxRadius.clear();
        }
    }

    private void addPoiToSearchResultIfNecessary(@NonNull LatLng latLng,
                                                 long minRadiusInMeters,
                                                 long maxRadiusInMeters,
                                                 float motionBearing,
                                                 @NonNull Poi poi,
                                                 @NonNull LatLng poiLatLng,
                                                 boolean hadPoi,
                                                 @NonNull PoiSearchResult searchResult) {
        Location.distanceBetween(latLng.latitude, latLng.longitude,
                poiLatLng.latitude, poiLatLng.longitude, result);
        float diffBearing = Math.abs(motionBearing - result[1]);
        float distanceToPoi = result[0];
        addPoiToSearchResultIfNecessary(minRadiusInMeters, maxRadiusInMeters, distanceToPoi,
                diffBearing, poi, hadPoi, searchResult);
    }

    private void addPoiToSearchResultIfNecessary(long minRadiusInMeters,
                                                 long maxRadiusInMeters,
                                                 float distanceToPoi,
                                                 float diffBearing,
                                                 @NonNull Poi poi,
                                                 boolean hadPoi,
                                                 @NonNull PoiSearchResult searchResult) {
        if (distanceToPoi <= minRadiusInMeters) {
            searchResult.withinMinRadius.put(new PoiInfo(distanceToPoi, diffBearing), poi);
        } else if (distanceToPoi <= maxRadiusInMeters) {
            searchResult.betweenMinAndMaxRadius.put(new PoiInfo(distanceToPoi, diffBearing), poi);
        } else {
            if (hadPoi) {
                searchResult.outOfMaxRadius.put(new PoiInfo(distanceToPoi, diffBearing), poi);
            }
        }
    }
}
