package ru.mhistory.screen.map.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import api.vo.Poi;
import ru.mhistory.R;
import ru.mhistory.common.util.UiUtil;
import ru.mhistory.geo.LatLng;

public class MhMapView extends FrameLayout implements GoogleMap.OnMarkerClickListener,GoogleMap.OnCameraMoveListener {
    public GoogleMap googleMap;
    private MapView mapViewDelegate;

    private final Rect mapBounds = new Rect();
    private Marker myLocationMarker;
    private boolean myLocationEnabled = false;
    private Circle searchingRadiusCircle;
    private Location myLocation;
    private boolean showSearchingRadius = false;
    private int searchingRadiusMeters;
    private Poi currentPoi;
    private Marker currentPoiMarker;
    private Circle currentPoiCircle;
    private Map<LatLng, Integer> audioIndexCache = new HashMap<>();
    private Map<LatLng, Poi> poiCache = new HashMap<>();
    private Map<LatLng, MarkerOptions> markerOptions = new HashMap<>();
    private Map<LatLng, Marker> markers = new HashMap<>();
    private boolean showPoi;
    private BitmapDescriptor poiMarkerIcon;

    public MhMapView(Context context) {
        this(context, null);
    }

    public MhMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MhMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        mapViewDelegate = new MapView(context);
        addView(mapViewDelegate);
    }

    public final void onCreate(@NonNull Bundle savedState) {
        mapViewDelegate.onCreate(savedState);
    }

    public final void onStart() {
        mapViewDelegate.onStart();
    }

    public final void onStop() {
        mapViewDelegate.onStop();
    }

    public final void onResume() {
        mapViewDelegate.onResume();
    }

    public final void onPause() {
        mapViewDelegate.onPause();
    }

    public final void onDestroy() {
        mapViewDelegate.onDestroy();
    }

    public final void onLowMemory() {
        mapViewDelegate.onLowMemory();
    }

    public final void onSaveInstanceState(@NonNull Bundle savedState) {
        mapViewDelegate.onSaveInstanceState(savedState);
    }

    @Override
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        super.measureChildren(widthMeasureSpec, heightMeasureSpec);
        mapBounds.set(0, 0, mapViewDelegate.getMeasuredWidth(),
                mapViewDelegate.getMeasuredHeight());
    }

    @UiThread
    public void getMapAsync(@NonNull final OnMapReadyCallback callback) {
        mapViewDelegate.getMapAsync(googleMap -> {
            MhMapView.this.googleMap = googleMap;
            MhMapView.this.googleMap.setOnMarkerClickListener(MhMapView.this);
            MhMapView.this.googleMap.setOnCameraMoveListener(this);
            callback.onMapReady(googleMap);
        });
    }

    public void setMyLocationEnabled(boolean enabled) {
        myLocationEnabled = enabled;
        if (!enabled) {
            if (myLocationMarker != null) {
                myLocationMarker.remove();
                myLocationMarker = null;
            }
            if (searchingRadiusCircle != null) {
                searchingRadiusCircle.remove();
                searchingRadiusCircle = null;
            }
        }
    }

    public void setMyLocation(@NonNull Location location) {
        if (googleMap == null || !myLocationEnabled) {
            return;
        }
        this.myLocation = location;
        com.google.android.gms.maps.model.LatLng latLng
                = new com.google.android.gms.maps.model.LatLng(location.getLatitude(),
                location.getLongitude());
        if (myLocationMarker == null) {
            Bitmap bitmap = UiUtil.drawableToBitmap(getContext(), R.drawable.my_location);
            myLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        } else {
            myLocationMarker.setPosition(latLng);
        }
        showSearchingRadiusIfNecessary();
    }

    public void clearPoi() {
        currentPoi = null;
        currentPoiMarker = null;
        showPoi = false;
        removePoiMarkers();
        poiCache.clear();
        audioIndexCache.clear();
        if (currentPoiCircle != null) {
            currentPoiCircle.remove();
            currentPoiCircle = null;
        }
    }

    public void setShowSearchingRadius(boolean show) {
        if (showSearchingRadius == show) {
            return;
        }
        showSearchingRadius = show;
        showSearchingRadiusIfNecessary();
    }

    public void setSearchingRadius(int searchingRadiusMeters) {
        this.searchingRadiusMeters = searchingRadiusMeters;
        showSearchingRadiusIfNecessary();
    }

    private void showSearchingRadiusIfNecessary() {
        if (myLocation == null) {
            return;
        }
        if (showSearchingRadius) {
            if (searchingRadiusMeters == 0) {
                return;
            }
            com.google.android.gms.maps.model.LatLng latLng
                    = new com.google.android.gms.maps.model.LatLng(myLocation.getLatitude(),
                    myLocation.getLongitude());
            if (searchingRadiusCircle == null) {
                searchingRadiusCircle = googleMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(searchingRadiusMeters)
                        .strokeWidth(UiUtil.dipToPx(getContext(), 1))
                        .strokeColor(ContextCompat.getColor(getContext(), R.color.accuracy_stroke))
                        .fillColor(ContextCompat.getColor(getContext(), R.color.accuracy_fill))
                        .clickable(false));
            } else {
                searchingRadiusCircle.setCenter(latLng);
                searchingRadiusCircle.setRadius(searchingRadiusMeters);
            }
        } else {
            if (searchingRadiusCircle != null) {
                searchingRadiusCircle.remove();
                searchingRadiusCircle = null;
            }
        }
    }

    public void setShowPoi(boolean show) {
        if (showPoi == show) {
            return;
        }
        showPoi = show;
        showPoiIfNecessary();
    }

    public void setPoi(@NonNull Map<LatLng, Poi> pois) {
        this.poiCache = pois;
        if (pois.isEmpty()) {
            return;
        }
        showPoiIfNecessary();
    }

    private void showPoiIfNecessary() {
        removePoiMarkers();
        if (showPoi) {
            fillPoiMarkerOptions();
            for (Map.Entry<ru.mhistory.geo.LatLng, MarkerOptions> entry : markerOptions.entrySet()) {
                Marker poiMarker = googleMap.addMarker(entry.getValue());
                poiMarker.setTag(entry.getKey());
                 markers.put(entry.getKey(), poiMarker);
            }
        } else {
            if (currentPoi != null) {
                ru.mhistory.geo.LatLng latLng = new ru.mhistory.geo.LatLng(currentPoi.latitude,
                        currentPoi.longitude);
                MarkerOptions markerOptions = createPoiMarkerOptions(latLng.latitude,
                        latLng.longitude, currentPoi.name, currentPoi.full_name,
                        getAudioIndexCache(latLng),
                        currentPoi.size());
                currentPoiMarker = googleMap.addMarker(markerOptions);
                currentPoiMarker.setTag(latLng);
                markers.put(latLng, currentPoiMarker);
                currentPoiMarker.showInfoWindow();
            }
        }
    }

    private void fillPoiMarkerOptions() {
        for (Map.Entry<LatLng, Poi> poiEntry : poiCache.entrySet()) {
            LatLng latLng = poiEntry.getKey();
            Poi poi = poiEntry.getValue();
            MarkerOptions markerOptions = createPoiMarkerOptions(latLng.latitude,
                    latLng.longitude, poi.name, poi.full_name, getAudioIndexCache(latLng),
                    poi.size());
            this.markerOptions.put(latLng, markerOptions);
        }
    }

    private int getAudioIndexCache(@NonNull LatLng latLng) {
        Integer audioIndex = audioIndexCache.get(latLng);
        return audioIndex != null ? audioIndex : 0;
    }

    private void removePoiMarkers() {
        markerOptions.clear();
        if (markers.isEmpty()) {
            return;
        }
        for (Marker marker : markers.values()) {
            marker.remove();
        }
        markers.clear();
    }

    @NonNull
    protected MarkerOptions createPoiMarkerOptions(double latitude,
                                                   double longitude,
                                                   String title,
                                                   String snippet,
                                                   int audioIndex,
                                                   int audioCount) {
        ensurePoiMarkerIcon();
        return new MarkerOptions()
                .position(new com.google.android.gms.maps.model.LatLng(latitude, longitude))
                .anchor(0.5f, 1f)
                .title(String.format(Locale.getDefault(), "%s; %d/%d", title, audioIndex,
                        audioCount))
                .snippet(snippet)
                .icon(poiMarkerIcon);
    }

    private void ensurePoiMarkerIcon() {
        if (poiMarkerIcon == null) {
            poiMarkerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_pin);
        }
    }

    public void onPoiFound(@NonNull Poi poi) {
        ru.mhistory.geo.LatLng latLng = new ru.mhistory.geo.LatLng(poi.latitude, poi.longitude);
        currentPoi = poi;
        Marker poiMarker = markers.get(latLng);
        if (poiMarker != null) {
            currentPoiMarker = poiMarker;
            com.google.android.gms.maps.model.LatLng latLng1 =
                    new com.google.android.gms.maps.model.LatLng(latLng.latitude, latLng.longitude);
            if (currentPoiCircle == null) {
                currentPoiCircle = googleMap.addCircle(new CircleOptions()
                        .center(latLng1)
                        .radius(300)
                        .strokeWidth(UiUtil.dipToPx(getContext(), 1))
                        .strokeColor(ContextCompat.getColor(getContext(), R.color.accuracy_current_stroke))
                        .fillColor(ContextCompat.getColor(getContext(), R.color.accuracy_current_fill))
                        .clickable(false));
            } else {
                currentPoiCircle.setCenter(latLng1);
            }
            poiMarker.showInfoWindow();
        }
    }

    public void onPoiReleased() {
        currentPoi = null;
        if (currentPoiMarker != null) {
            currentPoiMarker = null;
            if (currentPoiCircle != null) {
                currentPoiCircle.remove();
                currentPoiCircle = null;
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
        } else {
            marker.showInfoWindow();
        }
        return true;
    }

    public void nextPoiTrackInfo() {
        if (currentPoiMarker != null) {
            ru.mhistory.geo.LatLng latLng = new ru.mhistory.geo.LatLng(currentPoi.latitude,
                    currentPoi.longitude);
            int audioIndex = getAudioIndexCache(latLng);
            audioIndex++;
            currentPoiMarker.setTitle(String.format(Locale.getDefault(), "%s; %d/%d",
                    currentPoi.name, audioIndex, currentPoi.size()));
            audioIndexCache.put(latLng, audioIndex);
            currentPoiMarker.hideInfoWindow();
            currentPoiMarker.showInfoWindow();
        }
    }

    @Override
    public void onCameraMove() {
//        float angle=MhMapView.this.googleMap.getCameraPosition().bearing;
//        float z=  MhMapView.this.googleMap.getCameraPosition().zoom;
//        Toast.makeText(this.getContext(),"CameraMove andle=" +angle + " zoom="+z,Toast.LENGTH_SHORT).show();


    }

}