package ru.mhistory.geo;

public class LatLng {
    public final double longitude;
    public final double latitude;

    public LatLng(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
    public LatLng(com.google.android.gms.maps.model.LatLng gogleLatLng) {
        this.longitude = gogleLatLng.longitude;
        this.latitude = gogleLatLng.latitude;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LatLng that = (LatLng) o;
        return longitude == that.longitude && latitude == that.latitude;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(longitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public com.google.android.gms.maps.model.LatLng toGoogle() {
        return new com.google.android.gms.maps.model.LatLng(latitude,longitude);
    }


}
