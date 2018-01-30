package ru.mhistory.bus.event;

/**
 * Created by shcherbakov on 19.01.2018.
 */

public class MapButtonClick {
    public final static int  buttonPausePlay =1;
    public final static int buttonNextTrack=2;
    public final static int buttonNextPoi=3;
    public int type;

    public MapButtonClick(int type) {
        this.type = type;
    }
}
