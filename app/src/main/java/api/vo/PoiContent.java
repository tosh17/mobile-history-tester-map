package api.vo;

/**
 * Created by shcherbakov on 06.11.2017.
 */

public class PoiContent {
    public final long id;
    public final String name;
    public final String content_type;
    public final boolean isAudioFormat;
    public final String audio;

    public PoiContent(long id, String name, String content_type, boolean isAudioFormat, String audio) {
        this.id = id;
        this.name = name;
        this.content_type = content_type;
        this.isAudioFormat = isAudioFormat;
        this.audio = audio;
    }
}
