package ru.mhistory.providers;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import api.vo.Poi;
import api.vo.PoiContent;
import io.realm.RealmList;
import ru.mhistory.MobileHistoryApp;
import ru.mhistory.log.Logger;
import ru.mhistory.realm.PoiContentRealm;
import ru.mhistory.realm.PoiRealm;
import ru.mhistory.realm.RealmFactory;

/**
 * Created by shcherbakov on 24.11.2017.
 */

public class JsonToReal {
    private final String LogTag = "JsonPaeser";
    private Uri uri;

    public JsonToReal(Uri uri) {
        this.uri = uri;
    }

    public boolean doIt() {
        //todo return list<Poi>
        long startTime = System.currentTimeMillis();
        Logger.d("Starting to parse the story file, start time is (%s, ms)", startTime);
        JsonReader jr = null;
        try {
            jr = new JsonReader(getFileInputStreamReader());
            readFromJson(jr);
            return true;
        } catch (UnsupportedEncodingException e) {
            Log.e("PoiProvider", "Exception while parsing story json", e);
            return false;
        } catch (IOException e) {
            Log.e("PoiProvider", "Exception while parsing story json", e);
            return false;
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

    protected void readFromJson(@NonNull JsonReader jr) throws IOException {
        RealmList<PoiRealm> poi = new RealmList<>();
        List<PoiContentRealm> content = new LinkedList<>();
        Logger.i(LogTag, "start parse json realm");
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
                            content.add(new PoiContentRealm().fromPoiContent(obj_id, new PoiContent(contentId, contentName, contentType, wow, text, contentAudio)));
                          //  contents.add(new PoiContent(contentId, contentName, contentType, wow, text, contentAudio));
                        }
                        jr.endArray();//content array finish
                        break;
                }
            }
            jr.endObject();  //point finish
            poi.add(new PoiRealm().fromPoi(new Poi(obj_id, name, type, full_name, full_address, lon, lat)));

        }
        jr.endArray(); //point array finish
        jr.endObject();
        Logger.i(LogTag, "finish parse json");
        toRealm(poi, content);
    }

    private void toRealm(RealmList<PoiRealm> poi, List<PoiContentRealm> content) {
        RealmFactory factory = RealmFactory.getInstance();
        factory.savePoi(poi);
        factory.SaveContent(content);

    }

    @NonNull
    protected Reader getFileInputStreamReader() throws FileNotFoundException {
        InputStream is = MobileHistoryApp.getContext().getContentResolver().openInputStream(uri);
        if (is == null) {
            throw new IllegalStateException("Can't open stream from " + uri);
        }
        return new BufferedReader(new InputStreamReader(is));
    }
}
