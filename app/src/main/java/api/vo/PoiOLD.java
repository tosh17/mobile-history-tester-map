//package api.vo;
//
//import android.support.annotation.NonNull;
//
//import java.util.Collections;
//import java.util.List;
//
//public class Poi {
//    public final String name;
//    public final String desc;
//    public final double longitude;
//    public final double latitude;
//    public final List<String> audioUrls;
//
//    public Poi(@NonNull String name,
//               @NonNull String desc,
//               double longitude,
//               double latitude,
//               @NonNull List<String> audioUrls) {
//        this.name = name;
//        this.desc = desc;
//        this.longitude = longitude;
//        this.latitude = latitude;
//        this.audioUrls = Collections.unmodifiableList(audioUrls);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//        Poi poi = (Poi) o;
//        return Double.compare(poi.longitude, longitude) == 0
//                && Double.compare(poi.latitude, latitude) == 0
//                && name.equals(poi.name);
//    }
//
//    @Override
//    public int hashCode() {
//        int result;
//        long temp;
//        result = name.hashCode();
//        temp = Double.doubleToLongBits(longitude);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        temp = Double.doubleToLongBits(latitude);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "Poi{" +
//                "name='" + name + '\'' +
//                ", desc='" + desc + '\'' +
//                ", longitude=" + longitude +
//                ", latitude=" + latitude +
//                ", audioUrls=" + audioUrls +
//                '}';
//    }
//}