package ru.mhistory.geo;

import android.support.annotation.IntDef;

import static ru.mhistory.geo.LocationAccuracy.HIGH;
import static ru.mhistory.geo.LocationAccuracy.LOW;
import static ru.mhistory.geo.LocationAccuracy.LOWEST;
import static ru.mhistory.geo.LocationAccuracy.MEDIUM;

@IntDef({HIGH, MEDIUM, LOW, LOWEST})
public @interface LocationAccuracy {
    int HIGH = 0;
    int MEDIUM = 1;
    int LOW = 2;
    int LOWEST = 3;
}

