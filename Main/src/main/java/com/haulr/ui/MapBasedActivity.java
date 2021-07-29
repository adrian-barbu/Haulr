package com.haulr.ui;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.haulr.R;
import com.haulr.googleapi.MyLocation;

/**
 * @description     Map Based Activity
 * @author          Adrian
 */
public abstract class MapBasedActivity extends BaseActivity implements OnMapReadyCallback {

    // Map Control
    protected GoogleMap mMap;
    private LatLngBounds mMapBounds;
    protected LatLngBounds.Builder mBoundsBuilder;

    // Vancouber Location
    protected LatLng locationVancover = new LatLng(49.283105, -123.119227);

    @Override
    protected void onDestroy() {
        if (mMap != null)
            mMap.clear();

        if (mMapBounds != null)
            mMapBounds = null;

        System.gc();

        super.onDestroy();
    }

    /**
     *  Initialize Map Viewer
     */
    protected void initMapView() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapViewer);
        mapFragment.getMapAsync(this);

        mBoundsBuilder = new LatLngBounds.Builder();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Hide the zoom controls as the button panel will cover it.
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setMyLocationEnabled(true);

        // When there is no location data, then will be showed in Vancouver, Canada
        setFirstMapView();
    }

    /**
     * Must to override this function when user want to set map view when map is ready
     */
    public abstract void setFirstMapView();

    /**
     * Reset Map
     */
    protected void resetMap() {
        mMap.clear();   // Clear All Markers

        if (mBoundsBuilder != null) {
            mBoundsBuilder = null;
        }

        mBoundsBuilder = new LatLngBounds.Builder();
    }


//    /**
//     * Add Location on Map
//     *
//     * @param title
//     * @param latitude
//     * @param longitude
//     */
//    protected void addLocation(String title, double latitude, double longitude, float markerHue) {
//        LatLng location = new LatLng(latitude, longitude);
//        mMap.addMarker(new MarkerOptions()
//                .position(location)
//                .title(title)
//                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
//                .icon(BitmapDescriptorFactory.defaultMarker(markerHue)));
//
//        mBoundsBuilder.include(location);
//    }

    /**
     * Add Pickup Location on Map
     *
     * @param address
     * @param latitude
     * @param longitude
     */
    protected void addPickupLocation(String address, double latitude, double longitude) {
        String title = getString(R.string.pickup_location);
        //if (address != null && !address.isEmpty())
        //    title += String.format("%n%s", address);

        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .snippet(String.format("%n%s", address))
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup_maker)));

        mBoundsBuilder.include(location);
    }

    /**
     * Add DropOff Location on Map
     *
     * @param address
     * @param latitude
     * @param longitude
     */
    protected void addDropOffLocation(String address, double latitude, double longitude) {
        String title = getString(R.string.dropoff_location);
        //if (address != null && !address.isEmpty())
        //    title += String.format("%n%s", address);

        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .snippet(String.format("%n%s", address))
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_dropoff_marker)));

        mBoundsBuilder.include(location);
    }

    /**
     * Add My Location on Map
     *
     * @param latitude
     * @param longitude
     */
    protected void addMyLocation(double latitude, double longitude) {
        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(getString(R.string.my_position))
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
                .icon(BitmapDescriptorFactory.defaultMarker()));

        mBoundsBuilder.include(location);
    }

    /**
     * Move Camera To Special Location
     */
    protected void moveCameraWithZoom(int zoomLevel) {
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.mapViewer).getView();
        final CameraUpdate zoom = CameraUpdateFactory.zoomTo(zoomLevel);

        try {
            mMapBounds = mBoundsBuilder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBounds, 0));
            mMap.animateCamera(zoom, 1000, null);
        } catch (Exception e) {
            if (mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressWarnings("deprecation") // We use the new method when supported
                    @SuppressLint("NewApi") // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        try {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            } else {
                                mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }

                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBounds, 0));
                            mMap.animateCamera(zoom, 1000, null);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            }
        }
    }

    /**
     *  Move Camera With Bounding Areas
     *
     * @param padding
     */
    protected void moveCameraWithPadding(final int padding) {
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.mapViewer).getView();

        try {
            mMapBounds = mBoundsBuilder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBounds, padding));
        } catch (Exception e) {
            if (mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressWarnings("deprecation") // We use the new method when supported
                    @SuppressLint("NewApi") // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        try {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            } else {
                                mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }

                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBounds, padding));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    /**
     * Add My Location on Map
     */
    private void addMyLocation() {
        /**
         * Add My Position On Map Viewer
         */

        try {
            Location myLocation = MyLocation.getLastKnownLocation(this);
            if (myLocation != null) {
                LatLng myLoc = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                mBoundsBuilder.include(myLoc);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}