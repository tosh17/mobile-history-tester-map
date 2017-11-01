package ru.mhistory.bus.event;

public class SetMaxPoiRadiusEvent {
    public final int radiusMeters;

    public SetMaxPoiRadiusEvent(int radiusMeters) {
        this.radiusMeters = radiusMeters;
    }
}
