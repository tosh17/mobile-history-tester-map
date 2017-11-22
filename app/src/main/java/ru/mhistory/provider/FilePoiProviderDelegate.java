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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import api.vo.Poi;
import api.vo.PoiContent;
import ru.mhistory.geo.LatLng;
import ru.mhistory.log.Logger;

public abstract class FilePoiProviderDelegate {
    LinkedHashMap<LatLng, Poi> cache;
    private PoiFinder poiFinder;
    private String LogTag = "JsonPaeser";

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
        //todo return list<Poi>
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
        Logger.i(LogTag, "start parse json");
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
        jr.nextName();
        jr.nextInt(); // area
        jr.nextName();
        jr.nextInt(); // version
        jr.nextName(); // points
        jr.beginArray();

        while (jr.hasNext()) {
            jr.beginObject(); //point begin
            long obj_id = 0;
            String name = "";
            String type = "";
            String full_name = "";
            String full_address = "";
            String text = "";
            double lat = 0;
            double lon = 0;
            List<PoiContent> contents = new ArrayList<>();
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
                    case "content":
                        jr.beginArray();
                        while (jr.hasNext()) {
                            jr.beginObject();
                            long contentId = 0;
                            int wow = 0;
                            String contentName = "";
                            String contentType = "";
                            Map<String, String> contentAudio = new HashMap<>();
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
                                    case "wow":
                                        wow = jr.nextInt();
                                        break;
                                    case "text":
                                        text = jr.nextString();
                                        break;
                                    case "audio":
                                        jr.beginArray();
                                        while (jr.hasNext()) {
                                            jr.beginObject();
                                            String typeAudio = "";
                                            String mp3 = "";
                                            while (jr.hasNext()) {
                                                switch (jr.nextName()) {
                                                    case "type":
                                                        typeAudio = jr.nextString();
                                                        break;
                                                    case "mp3":
                                                        mp3 = jr.nextString();
                                                        break;
                                                }
                                            }
                                            jr.endObject();
                                            contentAudio.put(typeAudio, mp3);
                                        }
                                        jr.endArray();
                                }
                            }
                            jr.endObject(); //content finish
                            Logger.i(LogTag, "Load id=" + contentId + "   contentName" + contentName);
                            contents.add(new PoiContent(contentId, contentName, contentType, wow, text, contentAudio));
                        }
                        jr.endArray();//content array finish
                        break;
                }
            }
            jr.endObject();  //point finish
            cache.put(new LatLng(lat, lon), new Poi(obj_id, name, type, full_name, full_address, lon, lat, contents));
        }
        jr.endArray(); //point array finish
        jr.endObject();
        Logger.i(LogTag, "finish parse json");
        return cache;
    }
}
