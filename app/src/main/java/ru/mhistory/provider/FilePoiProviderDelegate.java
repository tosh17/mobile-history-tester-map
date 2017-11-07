package ru.mhistory.provider;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.JsonReader;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import api.vo.Poi;
import api.vo.PoiContent;
import ru.mhistory.geo.LatLng;
import ru.mhistory.log.Logger;

public abstract class FilePoiProviderDelegate {
    LinkedHashMap<LatLng, Poi> cache;
    private PoiFinder poiFinder;

    FilePoiProviderDelegate(@NonNull PoiFinder pointSearcher) {
        poiFinder = pointSearcher;
    }

    @WorkerThread
    @NonNull
    PoiSearchResult findPois(@NonNull LatLng latLng,
                             @NonNull PoiProviderConfig config,
                             @NonNull Set<Poi> inputPois) {
        if (cache == null) {
            cache = readFileCache();
            poiFinder.setPoiCache(cache);
        }
        return poiFinder.findPois(latLng, config, inputPois);
    }

    @NonNull
    LinkedHashMap<LatLng, Poi> readFileCache() {
        long startTime = System.currentTimeMillis();
        Logger.d("Starting to parse the story file, start time is (%s, ms)", startTime);
        JsonReader jr = null;
        try {
            jr = new JsonReader(getFileInputStreamReader());
            return readFromJson(jr);
        } catch (UnsupportedEncodingException e) {
            Log.e("PoiProvider", "Exception while parsing story json", e);
            return new LinkedHashMap<>();
        } catch (IOException e) {
            Log.e("PoiProvider", "Exception while parsing story json", e);
            return new LinkedHashMap<>();
        } finally {
            if (jr != null) {
                try {
                    jr.close();
                } catch (IOException ignored) {
                }
            }
            long finishTime = System.currentTimeMillis();
            Logger.d("Finished to parse story file, finish time (%s, ms), time diff (%s, ms)",
                    finishTime, finishTime - startTime);
        }
    }

    @NonNull
    protected abstract Reader getFileInputStreamReader() throws FileNotFoundException;

    @NonNull
    protected LinkedHashMap<LatLng, Poi> readFromJson(@NonNull JsonReader jr) throws IOException {
        LinkedHashMap<LatLng, Poi> cache = new LinkedHashMap<>();
        jr.beginObject();
        jr.nextName();
        jr.nextString(); // sightseeing overview
        jr.nextName();
        jr.nextString(); // signature
        jr.nextName();
        jr.nextString(); // description
        jr.nextName();
        jr.nextInt(); // activationRadius
        jr.nextName();  //+
        String strContentType = jr.nextString(); //+
        boolean contentsFormat = strContentType.equals("mp3");
        jr.nextName(); // points
        jr.beginArray();

        while (jr.hasNext()) {
            long obj_id = 0;  //+
            String name = "";
            String type = "";  //+
            // -    String desc = "";
            String full_name = ""; //+
            String full_address = ""; //+

            double lat = 0;
            double lon = 0;
            // -    List<String> audioUrls = new ArrayList<>();
            List<PoiContent> contents = new ArrayList<>();
            jr.beginObject();
            while (jr.hasNext()) {

                switch (jr.nextName()) {
                    case "trigger":
                        jr.skipValue();
                        break;
                    case "obj_id":
                        obj_id = jr.nextLong();
                        break;
                    case "name":
                        name = jr.nextString();
                        break;
//                    case "description":
//                        desc = jr.nextString();
//                        break;
                    case "type":
                        type = jr.nextString();
                        break;
                    case "full_name":
                        full_name = jr.nextString();
                        break;
                    case "full_address":
                        full_address = jr.nextString();
                        break;
                    case "lon":
                        lon = jr.nextDouble();
                        break;
                    case "lat":
                        lat = jr.nextDouble();
                        break;
//                    case "audio":
//                        jr.beginArray();
//                        while (jr.hasNext()) {
//                            audioUrls.add(jr.nextString());
//                        }
//                        jr.endArray();
//                        break;
                    case "content":
                        jr.beginArray();
                        while (jr.hasNext()) {
                            jr.beginObject();
                            long contentId = 0;
                            String contentName = "";
                            String contentType = "";
                            String contentAudio = "";
                            while (jr.hasNext()) {
                                switch (jr.nextName()) {
                                    case "id":
                                        contentId = jr.nextLong();
                                        break;
                                    case "name":
                                        contentName = jr.nextString();
                                        break;
                                    case "content_type":
                                        contentType = jr.nextString();
                                        break;
                                    case "audio":
                                        contentAudio = jr.nextString();
                                        break;
                                }
                            }
                            jr.endObject();
                            contents.add(new PoiContent(contentId, contentName, contentType, contentsFormat, contentAudio));
                        }
                        jr.endArray();
                        break;
                }
            }
            jr.endObject();
            //     cache.put(new LatLng(lat, lon), new Poi(name, desc, lon, lat, audioUrls));
            cache.put(new LatLng(lat, lon), new Poi(obj_id, name, type, full_name, full_address, lon, lat, contents));
        }
        jr.endArray();
        jr.endObject();
        return cache;
    }
}