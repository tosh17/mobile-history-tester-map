package ru.mhistory.bus.event;

import android.support.annotation.NonNull;


import java.util.Map;

import api.vo.Poi;
import ru.mhistory.geo.LatLng;

public class PoiCacheAvailableEvent {
    public final Map<LatLng, Poi> pois;

    public PoiCacheAvailableEvent(@NonNull Map<LatLng, Poi> pois) {
        this.pois = pois;
    }
}
