package ru.mhistory.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import java.util.Set;

import api.vo.Poi;
import ru.mhistory.geo.LatLng;

public class FilePoiProvider implements PoiProvider {
    private volatile FilePoiProviderDelegate delegate;
    private volatile PoiProviderConfig config;

    @UiThread
    public void setDelegate(@Nullable FilePoiProviderDelegate delegate) {
        this.delegate = delegate;
    }

    @WorkerThread
    @Nullable
    @Override
    public PoiSearchResult findPois(@NonNull LatLng latLng, @NonNull Set<Poi> inputPois) {
        FilePoiProviderDelegate delegate = this.delegate;
        return delegate != null ? delegate.findPois(latLng, config, inputPois) : null;
    }

    @UiThread
    @Override
    public void setProviderConfig(@NonNull PoiProviderConfig config) {
        this.config = config;
    }
}