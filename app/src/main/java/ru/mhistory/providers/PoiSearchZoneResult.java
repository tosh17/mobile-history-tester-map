package ru.mhistory.providers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.HashSet;
import java.util.Iterator;
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
    public Pair<PoiInfo, Poi> getNearestPoi(boolean isStay){
        if(isStay) return getNearestPoi();
        return getNearestZonePoi();
    }
    @Nullable
    public Pair<PoiInfo, Poi> getNearestPoi() {
        Map.Entry<PoiInfo, Poi> entry = null;
        if (stay.size() > 0) {
            entry = stay.entrySet().iterator().next();
        }
        return entry != null ? new Pair<>(entry.getKey(), entry.getValue()) : null;
    }

    @Nullable
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
    @Nullable
    public Pair<PoiInfo, Poi> getNextNearestPoi(boolean isStay,Set<Poi> last){
        if(last==null) return getNearestPoi(isStay);
        if(isStay) return getNextNearestPoi(last);
        return getNextNearestZonePoi(last);
    }
    @Nullable
    public Pair<PoiInfo, Poi> getNextNearestPoi(Set<Poi> last) {
        Map.Entry<PoiInfo, Poi> entry = null;

        Iterator<Map.Entry<PoiInfo, Poi>> iterator = stay.entrySet().iterator();
        while(iterator.hasNext()){
            entry=iterator.next();
            if(last.contains(entry.getValue())) entry = null;
            else break;
        }
        return entry != null ? new Pair<>(entry.getKey(), entry.getValue()) : null;
    }
    @Nullable
    public Pair<PoiInfo, Poi> getNextNearestZonePoi(Set<Poi> last) {
        Map.Entry<PoiInfo, Poi> entry = null;

        Iterator<Map.Entry<PoiInfo, Poi>> iterator = zone1.entrySet().iterator();
        while(iterator.hasNext()){
            entry=iterator.next();
            if(last.contains(entry.getValue())) entry = null;
            else break;
        }
        if(entry!=null) return new Pair<>(entry.getKey(), entry.getValue());

         iterator = zone2.entrySet().iterator();
        while(iterator.hasNext()){
            entry=iterator.next();
            if(last.contains(entry.getValue())) entry = null;
            else break;
        }
        if(entry!=null) return new Pair<>(entry.getKey(), entry.getValue());

        iterator = zone3.entrySet().iterator();
        while(iterator.hasNext()){
            entry=iterator.next();
            if(last.contains(entry.getValue())) entry = null;
            else break;
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

    public String moveToString(float currentAngle) {
        StringBuilder str = new StringBuilder("<br><br>");
        float angle;
        str.append("<font color=#21610B>");
        str.append("Статус Движение:<br>");
        str.append("</font>");
        str.append("<font color=#DF01D7>");
        str.append("Zona 0:<br>");
        for (PoiInfo p : zone0.keySet()) {
            angle = currentAngle-p.angle;
            str.append(zone0.get(p).name + " distanse=" + p.distanceTo + " Angle= " + angle + "<br>");
        }
        str.append("</font>");
        str.append("<font color=#04B431>");
        str.append("Zona 1:<br>");
        for (PoiInfo p : zone1.keySet()) {
//            angle = Math.abs(p.angle - currentAngle);
//            angle = angle > 180 ? (angle - 360) : angle;
            angle = currentAngle-p.angle;
            str.append(zone1.get(p).name + " distanse=" + p.distanceTo + " Angle= " + angle + "<br>");
        }
        str.append("</font>");
        str.append("<font color=#0404B4>");
        str.append("Zona 2:<br>");
        for (PoiInfo p : zone2.keySet()) {
            angle = currentAngle-p.angle;
            str.append(zone2.get(p).name + " distanse=" + p.distanceTo + " Angle= " + angle + "<br>");
        }
        str.append("</font>");
        str.append("<font color=#FF8000>");
        str.append("Zona 3:<br>");
        for (PoiInfo p : zone3.keySet()) {
            angle = currentAngle-p.angle;
            str.append(zone3.get(p).name + " distanse=" + p.distanceTo + " Angle= " + angle + "<br>");
        }
        str.append("</font>");
        return str.toString();
    }

    public String stayToString() {
        StringBuilder str = new StringBuilder("\n\n");
        str.append("Статус Остановка:<br>");
        for (PoiInfo p : stay.keySet())
            str.append(stay.get(p).name + " distanse=" + p.distanceTo + "<br>");
        return str.toString();
    }

    public boolean contains(Poi p, boolean isStay) {
        if (isStay) return stay.containsValue(p);
        else
            return zone1.containsValue(p) || zone2.containsValue(p) || zone3.containsValue(p) || zone0.containsValue(p);
    }
}
