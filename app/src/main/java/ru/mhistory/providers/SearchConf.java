package ru.mhistory.providers;

/**
 * Created by shcherbakov on 26.11.2017.
 */

public class SearchConf {
    public int searchSquare = 100000;
    public int reSearchSquare = 50000;
    public int deltaDistanceToTracking = 5;
    public int radiusStay = 10000;
    public int speedToMove=1;
    public int radiusZone1 = 500;
    public float movementAngle = 0;
    public float deltaAngleZona2 = 45;
    public int radiusZone2 = 2000;
    public float deltaAngleZona3 = 120;

    public SearchConf() {
    }

}
