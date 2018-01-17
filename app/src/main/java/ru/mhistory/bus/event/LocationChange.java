package ru.mhistory.bus.event;

import android.location.Location;

import ru.mhistory.geo.LatLng;

/**
 * Created by shcherbakov on 09.01.2018.
 */

public class LocationChange {
    public LatLng location;
    public float angle;

    public LocationChange(LatLng location, float angle) {
        this.location = location;
        this.angle = angle;
    }
}
