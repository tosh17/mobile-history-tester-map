package api.vo;

import android.support.annotation.NonNull;

public class PoiInfo implements Comparable<PoiInfo> {
    public final float distanceTo;
    public final float angle;

    public PoiInfo(float distanceTo, float angle) {
        this.angle = angle;
        this.distanceTo = distanceTo;
    }

    @Override
    public int compareTo(@NonNull PoiInfo another) {
        return distanceTo < another.distanceTo
                ? -1
                : (distanceTo > another.distanceTo
                ? 1
                : (angle < another.angle
                ? -1
                : (angle > another.angle ? 1 : 0)));
    }
}
