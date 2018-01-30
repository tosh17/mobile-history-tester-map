package ru.mhistory.bus.event;

/**
 * Created by shcherbakov on 19.01.2018.
 */

public class MapButtonState {
    static public final int BTN_NEXT_TRACK=1;

    public int btn;
    public boolean stare;

    public MapButtonState(int btn, boolean stare) {
        this.btn = btn;
        this.stare = stare;
    }
}
