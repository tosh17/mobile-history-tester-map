package api.vo;

import android.location.Location;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ru.mhistory.geo.LatLng;

/**
 * Created by shcherbakov on 10.01.2018.
 */

public class AngleAvgLatLng {

    public int count;
    private List<LatLng> avg = new LinkedList<>();

    public AngleAvgLatLng(int count) {
        this.count = count;
    }

    public float add(LatLng latLng) {
        avg.add(latLng);
        float result = 0;
        float[] temp = new float[2];
        Iterator iterator = avg.iterator();
        LatLng first = (LatLng) iterator.next();
        while (iterator.hasNext()) {
            LatLng next = (LatLng) iterator.next();
            Location.distanceBetween(first.latitude, first.longitude,
                    next.latitude, next.longitude,
                    temp);
            result += temp[1];
        }
        if (avg.size() >= count) avg.remove(0);
        return avg.size() == 1 ? 0 : result / (count - 1);
    }

    public int size() {
        return avg.size();
    }
}
