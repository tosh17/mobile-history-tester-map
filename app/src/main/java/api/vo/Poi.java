package api.vo;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Poi {

    public final long objId;  //+
    public final String name;
    public final String type;  //+
    public final String full_name; //+
    public final String full_address; //+
    public final double longitude;
    public final double latitude;
    private List<PoiContent> contents;
    private int currentContent=-1;

    public int status=0;
    public Poi(long objId,@NonNull String name,
               @NonNull String type,@NonNull String full_name,@NonNull String full_address,
               double longitude,
               double latitude,
               @NonNull List<PoiContent> contents) {
        this.objId=objId;
        this.name = name;
        this.type=type;
        this.full_name=full_name;
        this.full_address=full_address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.contents = Collections.unmodifiableList(contents);
    }
    public Poi(long objId,@NonNull String name,
               @NonNull String type,@NonNull String full_name,@NonNull String full_address,
               double longitude,
               double latitude) {
        this.objId=objId;
        this.name = name;
        this.type=type;
        this.full_name=full_name;
        this.full_address=full_address;
        this.longitude = longitude;
        this.latitude = latitude;
        }



    public Poi addContent(PoiContent content){
      if(contents==null) contents=new ArrayList<>()  ;
       contents.add(content);
       return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Poi poi = (Poi) o;
        return Double.compare(poi.longitude, longitude) == 0
                && Double.compare(poi.latitude, latitude) == 0
                && name.equals(poi.name);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name.hashCode();
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Poi{" +
                "id='" + objId + '\'' +
                "name='" + name + '\'' +
                "type='" + type + '\'' +
                ", full_name='" + full_name + '\'' +
                ", full_address='" + full_address + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", audioSize=" + contents.size() +
                '}';
    }

    public PoiContent getNextContent(){
        //todo добавить wow  локику
       return contents.get(++currentContent);
    }
    public PoiContent getCurrentContent(){
        //todo добавить wow  локику
        return contents.get(currentContent);
    }
    public boolean isHasNextContent(){
        return currentContent<contents.size()-1;
    }

    public int size(){ return contents.size();}

    public List<PoiContent> getAllContent() {
        return contents;
    }
}