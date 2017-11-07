//package ru.mhistory.provider;
//
//import android.support.annotation.NonNull;
//import android.support.annotation.WorkerThread;
//import android.util.JsonReader;
//import android.util.Log;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.Reader;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Set;
//
//import api.vo.Poi;
//import ru.mhistory.geo.LatLng;
//import ru.mhistory.log.Logger;
//
//public abstract class FilePoiProviderDelegate {
//    LinkedHashMap<LatLng, Poi> cache;
//    private PoiFinder poiFinder;
//
//    FilePoiProviderDelegate(@NonNull PoiFinder pointSearcher) {
//        poiFinder = pointSearcher;
//    }
//
//    @WorkerThread
//    @NonNull
//    PoiSearchResult findPois(@NonNull LatLng latLng,
//                             @NonNull PoiProviderConfig config,
//                             @NonNull Set<Poi> inputPois) {
//        if (cache == null) {
//            cache = readFileCache();
//            poiFinder.setPoiCache(cache);
//        }
//        return poiFinder.findPois(latLng, config, inputPois);
//    }
//
//    @NonNull
//    LinkedHashMap<LatLng, Poi> readFileCache() {
//        long startTime = System.currentTimeMillis();
//        Logger.d("Starting to parse the story file, start time is (%s, ms)", startTime);
//        JsonReader jr = null;
//        try {
//            jr = new JsonReader(getFileInputStreamReader());
//            return readFromJson(jr);
//        } catch (UnsupportedEncodingException e) {
//            Log.e("PoiProvider", "Exception while parsing story json", e);
//            return new LinkedHashMap<>();
//        } catch (IOException e) {
//            Log.e("PoiProvider", "Exception while parsing story json", e);
//            return new LinkedHashMap<>();
//        } finally {
//            if (jr != null) {
//                try {
//                    jr.close();
//                } catch (IOException ignored) {
//                }
//            }
//            long finishTime = System.currentTimeMillis();
//            Logger.d("Finished to parse story file, finish time (%s, ms), time diff (%s, ms)",
//                    finishTime, finishTime - startTime);
//        }
//    }
//
//    @NonNull
//    protected abstract Reader getFileInputStreamReader() throws FileNotFoundException;
//
//    @NonNull
//    protected LinkedHashMap<LatLng, Poi> readFromJson(@NonNull JsonReader jr) throws IOException {
//        LinkedHashMap<LatLng, Poi> cache = new LinkedHashMap<>();
//        jr.beginObject();
//        jr.nextName();
//        jr.nextString(); // sightseeing overview
//        jr.nextName();
//        jr.nextString(); // signature
//        jr.nextName();
//        jr.nextString(); // description
//        jr.nextName();
//        jr.nextInt(); // activationRadius
//        jr.nextName(); // points
//        jr.beginArray();
//
//        while (jr.hasNext()) {
//            String name = "";
//            String desc = "";
//            double lat = 0;
//            double lon = 0;
//            List<String> audioUrls = new ArrayList<>();
//            jr.beginObject();
//            while (jr.hasNext()) {
//                switch (jr.nextName()) {
//                    case "trigger":
//                        jr.skipValue();
//                        break;
//                    case "name":
//                        name = jr.nextString();
//                        break;
//                    case "description":
//                        desc = jr.nextString();
//                        break;
//                    case "lon":
//                        lon = jr.nextDouble();
//                        break;
//                    case "lat":
//                        lat = jr.nextDouble();
//                        break;
//                    case "audio":
//                        jr.beginArray();
//                        while (jr.hasNext()) {
//                            audioUrls.add(jr.nextString());
//                        }
//                        jr.endArray();
//                        break;
//                }
//            }
//            jr.endObject();
//            cache.put(new LatLng(lat, lon), new Poi(name, desc, lon, lat, audioUrls));
//        }
//        jr.endArray();
//        jr.endObject();
//        return cache;
//    }
//}
