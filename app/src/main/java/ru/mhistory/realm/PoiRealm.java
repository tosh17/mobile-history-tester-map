//package ru.mhistory.realm;
//
//import java.util.List;
//
//import api.vo.Poi;
//import api.vo.PoiContent;
//import io.realm.RealmObject;
//import io.realm.annotations.PrimaryKey;
//
///**
// * Created by shcherbakov on 18.11.2017.
// */
//
//public class PoiRealm extends RealmObject {
//    @PrimaryKey
//    public  long objId;
//    public  String name;
//    public  String type;
//    public  String full_name;
//    public  String full_address;
//    public  double longitude;
//    public  double latitude;
//    public  long[] contents;
//    public PoiRealm() {}
//    public void fromPoi(Poi poi) {
//        this.objId=poi.objId;
//        this.name=poi.name;
//        this.type=poi.type;
//        this.full_name=poi.full_name;
//        this.full_address=poi.full_address;
//        this.longitude=poi.longitude;
//        this.latitude=poi.latitude;
//        contents=new long[poi.contents.size()];
//        int i=0;
//        for(PoiContent c:poi.contents) contents[i++]=c.id;
//    }
//
//    public long getObjId() {
//        return objId;
//    }
//
//    public PoiRealm setObjId(long objId) {
//        this.objId = objId;
//        return this;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public PoiRealm setName(String name) {
//        this.name = name;
//        return this;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public PoiRealm setType(String type) {
//        this.type = type;
//        return this;
//    }
//
//    public String getFull_name() {
//        return full_name;
//    }
//
//    public PoiRealm setFull_name(String full_name) {
//        this.full_name = full_name;
//        return this;
//    }
//
//    public String getFull_address() {
//        return full_address;
//    }
//
//    public PoiRealm setFull_address(String full_address) {
//        this.full_address = full_address;
//        return this;
//    }
//
//    public double getLongitude() {
//        return longitude;
//    }
//
//    public PoiRealm setLongitude(double longitude) {
//        this.longitude = longitude;
//        return this;
//    }
//
//    public double getLatitude() {
//        return latitude;
//    }
//
//    public PoiRealm setLatitude(double latitude) {
//        this.latitude = latitude;
//        return this;
//    }
//
//    public long[] getContents() {
//        return contents;
//    }
//
//    public PoiRealm setContents(long[] contents) {
//        this.contents = contents;
//        return this;
//    }
//}
