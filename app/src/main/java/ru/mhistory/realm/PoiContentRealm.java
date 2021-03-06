package ru.mhistory.realm;

import java.util.HashMap;
import java.util.Map;

import api.vo.PoiContent;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by shcherbakov on 18.11.2017.
 */

public class PoiContentRealm extends RealmObject {
    @PrimaryKey
    public  long id;
    public long idPoi;
    public  String name;
    public  String content_type;
    public  int wow;
    public String keyAudio;
    public  String audio;
    public  String text;

    public PoiContentRealm(){}
    public PoiContentRealm fromPoiContent(long idPoi,PoiContent poiContent) {
        this.id=poiContent.id;
        this.idPoi=idPoi;
        this.name=poiContent.name;
        this.content_type=poiContent.content_type;
        this.wow=poiContent.wow;
        this.text=poiContent.text;
        int i=0;
        int size=poiContent.audio.size();
        String tepmKey[]=new String[size];
        String tempAudio[]=new String[size];
        for(String key:poiContent.audio.keySet()){
            tepmKey[i]=key;
            tempAudio[i++]=poiContent.audio.get(key);
        }
        this.keyAudio=RealmFactory.listToStr(tepmKey);
        this.audio=RealmFactory.listToStr(tempAudio);
        return this;
    }

    public long getId() {
        return id;
    }

    public PoiContentRealm setId(long id) {
        this.id = id;
        return this;
    }

    public long getIdPoi() {
        return idPoi;
    }

    public PoiContentRealm setIdPoi(long idPoi) {
        this.idPoi = idPoi;
        return this;
    }

    public String getName() {
        return name;
    }

    public PoiContentRealm setName(String name) {
        this.name = name;
        return this;
    }

    public String getContent_type() {
        return content_type;
    }

    public PoiContentRealm setContent_type(String content_type) {
        this.content_type = content_type;
        return this;
    }

    public int getWow() {
        return wow;
    }

    public PoiContentRealm setWow(int wow) {
        this.wow = wow;
        return this;
    }

    public String getKeyAudio() {
        return keyAudio;
    }

    public PoiContentRealm setKeyAudio(String keyAudio) {
        this.keyAudio = keyAudio;
        return this;
    }

    public String getAudio() {
        return audio;
    }

    public PoiContentRealm setAudio(String audio) {
        this.audio = audio;
        return this;
    }

    public String getText() {
        return text;
    }

    public PoiContentRealm setText(String text) {
        this.text = text;
        return this;
    }

    public PoiContent toPoiContent() {
        Map<String,String> mapAudio=new HashMap<>();
        String [] keyArr=keyAudio.split(";");
        String [] audioArr=audio.split(";");

        for(int i=0;i<keyArr.length;i++){
            mapAudio.put(keyArr[i],audioArr[i]);
        }
        return new PoiContent(id,name,content_type,wow,text,mapAudio);
    }
}
