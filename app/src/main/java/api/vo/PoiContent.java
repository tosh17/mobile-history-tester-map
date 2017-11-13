package api.vo;

import java.util.Map;

/**
 * Created by shcherbakov on 06.11.2017.
 */

public class PoiContent {
    public final long id;
    public final String name;
    public final String content_type;
    public final int wow;
    public final Map<String,String> audio;

    public PoiContent(long id, String name, String content_type, int wow, Map<String,String> audio) {
        this.id = id;
        this.name = name;
        this.content_type = content_type;
        this.wow = wow;
        this.audio = audio;
    }

    public String getAudioTrack() {
        return audio.get("robot");
    }

    public String getAudioType() {
        if(id%2==0) return "tts";
        return "mp3";
    }
}
