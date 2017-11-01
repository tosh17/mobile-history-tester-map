package ru.mhistory.provider;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import api.vo.Poi;
import ru.mhistory.bus.BusProvider;
import ru.mhistory.bus.event.PoiCacheAvailableEvent;
import ru.mhistory.common.util.ThreadUtil;
import ru.mhistory.geo.LatLng;


abstract class PoiFinder {
    Map<LatLng, Poi> cache;

    @NonNull
    final PoiSearchResult findPois(@NonNull LatLng latLng,
                                   @NonNull PoiProviderConfig config,
                                   @NonNull Set<Poi> inputPois) {
        if (cache == null) {
            throw new IllegalStateException("Searcher not initialized");
        }
        return findPois(latLng, config.getMinRadiusInMeters(), config.getMaxRadiusInMeters(),
                config.getAngle(), inputPois);
    }

    @NonNull
    protected abstract PoiSearchResult findPois(
            @NonNull LatLng latLng,
            long minRadiusInMeters,
            long maxRadiusInMeters,
            float angle,
            @NonNull Set<Poi> inputPois);

    void setPoiCache(@NonNull Map<LatLng, Poi> cache) {
        this.cache = cache;
        sendPoiCacheAvailableEvent(new HashMap<>(cache));
    }

    private void sendPoiCacheAvailableEvent(@NonNull Map<LatLng, Poi> cache) {
        ThreadUtil.runOnUiThread(() ->
                BusProvider.getInstance().post(new PoiCacheAvailableEvent(cache)));
    }

}
