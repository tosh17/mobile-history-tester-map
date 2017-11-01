package ru.mhistory.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import java.util.Set;

import api.vo.Poi;
import ru.mhistory.geo.LatLng;

public interface PoiProvider {
    @WorkerThread
    @Nullable
    PoiSearchResult findPois(@NonNull LatLng latLng, @NonNull Set<Poi> inputPois);

    @UiThread
    void setProviderConfig(@NonNull PoiProviderConfig config);
}