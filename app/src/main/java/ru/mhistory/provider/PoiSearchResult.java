package ru.mhistory.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import api.vo.Poi;
import api.vo.PoiInfo;

public final class PoiSearchResult {
    public final SortedMap<PoiInfo, Poi> withinMinRadius = new TreeMap<>();
    public final SortedMap<PoiInfo, Poi> betweenMinAndMaxRadius = new TreeMap<>();
    public final SortedMap<PoiInfo, Poi> outOfMaxRadius = new TreeMap<>();

    @NonNull
    public Set<Poi> getAllPoi() {
        Set<Poi> pois = new HashSet<>();
        pois.addAll(withinMinRadius.values());
        pois.addAll(betweenMinAndMaxRadius.values());
        pois.addAll(outOfMaxRadius.values());
        return pois;
    }

    public void removeAll(@NonNull Set<Poi> poiToRemove) {
        withinMinRadius.values().removeAll(poiToRemove);
        betweenMinAndMaxRadius.values().removeAll(poiToRemove);
        outOfMaxRadius.values().removeAll(poiToRemove);
    }

    @Nullable
    public Pair<PoiInfo, Poi> getNearestPoi() {
        Map.Entry<PoiInfo, Poi> entry = null;
        if (withinMinRadius.size() > 0) {
            entry = withinMinRadius.entrySet().iterator().next();
        } else if (betweenMinAndMaxRadius.size() > 0) {
            entry = betweenMinAndMaxRadius.entrySet().iterator().next();
        } else if (outOfMaxRadius.size() > 0) {
            entry = outOfMaxRadius.entrySet().iterator().next();
        }
        return entry != null ? new Pair<>(entry.getKey(), entry.getValue()) : null;
    }

    public boolean isEmpty() {
        return withinMinRadius.size() == 0
                && betweenMinAndMaxRadius.size() == 0
                && outOfMaxRadius.size() == 0;
    }
}
