package ru.mhistory.bus.event;

import api.vo.Poi;

public class PoiFoundEvent {
    public final Poi poi;

    public PoiFoundEvent(Poi poi) {
        this.poi = poi;
    }
}
