package ru.mhistory.providers;

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

public final class PoiSearchZoneResult {
    public final SortedMap<PoiInfo, Poi> stay = new TreeMap<>();
    public final SortedMap<PoiInfo, Poi> zone0 = new TreeMap<>();
    public final SortedMap<PoiInfo, Poi> zone1 = new TreeMap<>();
    public final SortedMap<PoiInfo, Poi> zone2 = new TreeMap<>();
    public final SortedMap<PoiInfo, Poi> zone3 = new TreeMap<>();

    @NonNull
    public Set<Poi> getAllPoi() {
        Set<Poi> pois = new HashSet<>();
        pois.addAll(stay.values());
// todo return
        return pois;
    }

    public void removeAll(@NonNull Set<Poi> poiToRemove) {
        stay.values().removeAll(poiToRemove);
        zone0.values().removeAll(poiToRemove);
        zone1.values().removeAll(poiToRemove);
        zone2.values().removeAll(poiToRemove);
        zone3.values().removeAll(poiToRemove);
    }

    @Nullable
    public Pair<PoiInfo, Poi> getNearestPoi() {
        Map.Entry<PoiInfo, Poi> entry = null;
        if (stay.size() > 0) {
            entry = stay.entrySet().iterator().next();
        }
        return entry != null ? new Pair<>(entry.getKey(), entry.getValue()) : null;
    }

    public Pair<PoiInfo, Poi> getNearestZonePoi() {
        Map.Entry<PoiInfo, Poi> entry = null;
        if (zone1.size() > 0) {
            entry = zone1.entrySet().iterator().next();
        } else if (zone2.size() > 0) {
            entry = zone2.entrySet().iterator().next();
        } else if (zone3.size() > 0) {
            entry = zone3.entrySet().iterator().next();
        }
        return entry != null ? new Pair<>(entry.getKey(), entry.getValue()) : null;
    }

    public boolean isEmpty() {
        return stay.size() == 0;
    }

    public boolean isZoneEmpty() {
        return zone1.size() == 0
                && zone2.size() == 0
                && zone3.size() == 0;
    }

    public Pair<PoiInfo, Poi> getNearestPoi(boolean isStay) {
        if (isStay) return getNearestPoi();
        return getNearestZonePoi();
    }

    public boolean isEmpty(boolean isStay) {
        if (isStay) return isEmpty();
        return isZoneEmpty();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Zona Stay:\n");
        for (PoiInfo p : stay.keySet())
            str.append(stay.get(p).name + " distanse=" + p.distanceTo + "\n");
        str.append("Zona 1:\n");
        for (PoiInfo p : zone1.keySet())
            str.append(zone1.get(p).name + " distanse=" + p.distanceTo + "\n");
        str.append("Zona 2:\n");
        for (PoiInfo p : zone2.keySet())
            str.append(zone2.get(p).name + " distanse=" + p.distanceTo + "\n");
        str.append("Zona 3:\n");
        for (PoiInfo p : zone3.keySet())
            str.append(zone3.get(p).name + " distanse=" + p.distanceTo + "\n");
        return str.toString();
    }

    public String toString(float currentAngle) {
        StringBuilder str = new StringBuilder("Zona Stay:\n");
        float angle;
        for (PoiInfo p : stay.keySet()) {
            angle = currentAngle-p.angle;

            str.append(stay.get(p).name + " distanse=" + p.distanceTo + " Angle= " + angle + "\n");
        }
        str.append("Zona 1:\n");
        for (PoiInfo p : zone1.keySet()) {
            angle = Math.abs(p.angle - currentAngle);
            angle = angle > 180 ? (angle - 360) : angle;
            str.append(zone1.get(p).name + " distanse=" + p.distanceTo + " Angle= " + angle + "\n");
        }
        str.append("Zona 2:\n");
        for (PoiInfo p : zone2.keySet()) {
            angle = Math.abs(p.angle - currentAngle);
            angle = angle > 180 ? (angle - 360) : angle;
            str.append(zone2.get(p).name + " distanse=" + p.distanceTo + " Angle= " + angle + "\n");
        }
        str.append("Zona 3:\n");
        for (PoiInfo p : zone3.keySet()) {
            angle = Math.abs(p.angle - currentAngle);
            angle = angle > 180 ? (angle - 360) : angle;
            str.append(zone3.get(p).name + " distanse=" + p.distanceTo + " Angle= " + angle + "\n");
        }
        return str.toString();
    }
}
