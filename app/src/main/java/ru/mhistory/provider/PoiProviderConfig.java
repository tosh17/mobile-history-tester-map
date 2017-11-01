package ru.mhistory.provider;

public class PoiProviderConfig {
    public static int DEFAULT_MIN_RADIUS_METERS = 300;
    public static int DEFAULT_MAX_RADIUS_METERS = 10_000;
    public static float DEFAULT_ANGLE = 90;

    private volatile int minRadiusInMeters = DEFAULT_MIN_RADIUS_METERS;
    private volatile int maxRadiusInMeters = DEFAULT_MAX_RADIUS_METERS;
    private volatile float angle = DEFAULT_ANGLE;

    public PoiProviderConfig() {
    }

    public void setMinRadiusInMeters(int minRadiusInMeters) {
        this.minRadiusInMeters = minRadiusInMeters;
    }

    public void setMaxRadiusInMeters(int maxRadiusInMeters) {
        this.maxRadiusInMeters = maxRadiusInMeters;
    }

    public int getMinRadiusInMeters() {
        return minRadiusInMeters;
    }

    public int getMaxRadiusInMeters() {
        return maxRadiusInMeters;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }
}
