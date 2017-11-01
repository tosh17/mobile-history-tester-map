package ru.mhistory.provider;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import api.vo.Poi;
import ru.mhistory.MobileHistoryApp;
import ru.mhistory.R;
import ru.mhistory.geo.LatLng;

public class RawFilePoiProviderDelegate extends FilePoiProviderDelegate {
    private int index = -1;
    private List<Poi> cachePoi = new ArrayList<>();

    public RawFilePoiProviderDelegate(@NonNull PoiFinder poiFinder) {
        super(poiFinder);
    }

    @NonNull
    @WorkerThread
    public PoiSearchResult findPois(@NonNull LatLng latLng,
                                    @NonNull PoiProviderConfig config,
                                    @NonNull Set<Poi> inputPois) {
        index++;
        if (cache == null) {
            cache = readFileCache();
            cachePoi.addAll(cache.values());
        }
        if (index >= cachePoi.size()) {
            index = 0;
        }
        // TODO: do not return empty result
        return new PoiSearchResult();
    }

    @NonNull
    @Override
    protected Reader getFileInputStreamReader() {
        return new BufferedReader(
                new InputStreamReader(
                        MobileHistoryApp.getContext().getResources()
                                .openRawResource(R.raw.story)));
    }
}
