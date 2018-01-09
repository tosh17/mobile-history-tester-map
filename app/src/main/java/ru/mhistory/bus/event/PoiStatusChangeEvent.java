package ru.mhistory.bus.event;

import api.vo.Poi;

/**
 * Created by shcherbakov on 09.01.2018.
 */

public class PoiStatusChangeEvent {
    public final Poi poi;

    public PoiStatusChangeEvent(Poi poi) {
        this.poi = poi;
    }
}
