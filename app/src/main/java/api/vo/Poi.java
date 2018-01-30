package api.vo;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.mhistory.realm.RealmFactory;

public class Poi {

    public final long objId;
    public final String name;
    public final String type;
    public final String full_name;
    public final String full_address;
    public final double longitude;
    public final double latitude;
    private List<PoiContent> contents;
    private List<PoiContent> history=new ArrayList<>();
    private PoiContent currentContent;
    private PoiContent currentFlipContent;
    private int currentFlipPosition;
    private boolean isFlip=false;
    public int status = 0;

    public Poi(long objId, @NonNull String name,
               @NonNull String type, @NonNull String full_name, @NonNull String full_address,
               double longitude,
               double latitude,
               @NonNull List<PoiContent> contents) {
        this.objId = objId;
        this.name = name;
        this.type = type;
        this.full_name = full_name;
        this.full_address = full_address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.contents = Collections.unmodifiableList(contents);
    }

    public Poi(long objId, @NonNull String name,
               @NonNull String type, @NonNull String full_name, @NonNull String full_address,
               double longitude,
               double latitude) {
        this.objId = objId;
        this.name = name;
        this.type = type;
        this.full_name = full_name;
        this.full_address = full_address;
        this.longitude = longitude;
        this.latitude = latitude;
    }


    public Poi addContent(PoiContent content) {
        if (contents == null) contents = new ArrayList<>();
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

    public PoiContent getNextContent() {
        for (PoiContent content : contents)
            if (!history.contains(content)) {
                currentContent = content;
                return currentContent;
            }
        return null;
    }

    public PoiContent getCurrentContent() {
        //todo добавить wow  локику
        return currentContent;
    }
  public boolean isHasNext(){
       return contents.indexOf(currentContent)<size()-1;
  }
    public boolean isHasPrev(){
        return contents.indexOf(currentContent)>0;
    }
    public void setFlip(boolean flipVal) {
        if(isFlip==flipVal) {
            return;
        }
        else {
            isFlip=flipVal;}
        if (flipVal && currentContent != null){
            currentFlipPosition = contents.indexOf(currentContent);}
        else currentFlipPosition = -1;
    }

    public PoiContent getNextFlipContent() {
        return contents.get(++currentFlipPosition);
    }

    public PoiContent getPrevFlipContent() {
        return contents.get(--currentFlipPosition);
    }

    public PoiContent getCurrentFlipContent() {
        return contents.get(currentFlipPosition);
    }

    public boolean isHasNextFlipContent() {
        return currentFlipPosition < size() - 1;
    }

    public boolean isHasPrevFlipContent() {
        return currentFlipPosition > 0;
    }


    public boolean isThisPoiContent(PoiContent content) {
        return contents.contains(content);
    }

    public boolean putContentToHistory(long id) {
        return putContentToHistory(findContentByid(id));
    }
    public boolean putContentToHistory(long id, Context context) {
        PoiContent historyContent=findContentByid(id);
        if(!history.contains(historyContent)) {
            history.add(historyContent);
            RealmFactory.getInstance(context).saveHistory(this.objId,id);
        }
        return isPoiComplete();
    }
    public PoiContent findContentByid(long id) {
        for (PoiContent c : contents) {
            if (c.id == id) return c;
        }
        return null;
    }

    public boolean putContentToHistory(PoiContent content) {
        history.add(content);
        return isPoiComplete();
    }

    public boolean isPoiComplete() {
        return !(history.size() < size());
    }

    public int size() {
        return contents.size();
    }
    public int poiActiveStatus(){
        if(history.size()==0) return 1;
        if(isPoiComplete()) return 3;
        return 2;
    }
    public List<PoiContent> getAllContent() {
        return contents;
    }

    public int getCurrentPosition(){
        if(isFlip) return currentFlipPosition;
        if(currentContent==null) return 0;
        return contents.indexOf(currentContent);
    }

    public void clearHistory() {
        history.clear();
      status=0;
    }
}