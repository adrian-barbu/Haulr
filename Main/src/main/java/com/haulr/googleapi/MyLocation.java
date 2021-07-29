package com.haulr.googleapi;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @description     Module for getting my location
 * @author          Adrian
 */
public class MyLocation {

    Context mContext;

    boolean gpsEnabled=false;       // Flag whether the gps function is enabled
    boolean networkEnabled=false;   // Flag whether the network function is enabled

    LocationManager mLocManager;
    LocationListener mLocListener;

    Location mLocation = null;             // My Location

    public Location getLocation() { return mLocation; }

    private double mLatitude, mLongitude;     // My Location
    private String mName, mAddress, mState, mCity, mCountry;

    public String getName() { return mName; }
    public String getAddress() { return mAddress; }
    public String getState() { return mState; }
    public String getCity() { return mCity; }
    public String getCountry() { return mCountry; }

    public double getLatitude() { return mLatitude; }
    public double getLongitude() { return mLongitude; }

    private static MyLocation instance;

    public static MyLocation getInstance(Context context) {
        if (instance == null)
            instance = new MyLocation(context);

        if (instance.getLocation() == null)
            instance.getMyCurrentLocation();

        return instance;
    }

    public MyLocation(Context context) {
        mContext = context;

        mLocManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocListener = new MyLocationListener();
    }

    /**
     * Check the type of GPS Provider available at that instance and
     * collect the location informations
     *
     * @Output Latitude and Longitude
     */
    void getMyCurrentLocation() {
        try{gpsEnabled=mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
        try{networkEnabled=mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        //don't start listeners if no provider is enabled
        //if(!gpsEnabled && !networkEnabled)
        //return false;

        if (networkEnabled)
            mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocListener);
        else if (gpsEnabled)
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
        else
            return;

        /*
        if(gpsEnabled){
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
        }


        if(gpsEnabled){
            location=mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }


        if(networkEnabled && location==null){
            mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocListener);
        }


        if(networkEnabled && location==null)    {
            location=mLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }
        */
    }

    public void stopListener() {
        if (mLocManager != null)
            mLocManager.removeUpdates(mLocListener);
    }

    private void catchMyLocation() {
        if (mLocation != null) {
            mLatitude = mLocation.getLatitude();
            mLongitude = mLocation.getLongitude();

        } else {
            Location loc= getLastKnownLocation(mContext);
            if (loc != null) {
                mLatitude = loc.getLatitude();
                mLongitude = loc.getLongitude();
            }
        }

        try
        {
            // Getting address from found locations.
            Geocoder geocoder;

            List<Address> addresses;
            geocoder = new Geocoder(mContext, Locale.getDefault());
            addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);

            mName = addresses.get(0).getFeatureName();
            mState= addresses.get(0).getAdminArea();
            mCity = addresses.get(0).getLocality();
            mCountry = addresses.get(0).getCountryName();
            mAddress = String.format("%s, %s, %s, %s", addresses.get(0).getAddressLine(0), mCity, mState, mCountry);

            // you can get more details other than this . like country code, state code, etc.

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Location listener class. to get location.
    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            if (location != null) {
                mLocation = location;
                catchMyLocation();
                //mLocManager.removeUpdates(mLocListener);
            }
        }

        public void onProviderDisabled(String provider) {
            int a;
            a = 1;
        }

        public void onProviderEnabled(String provider) {
            int a;
            a = 1;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            int a;
            a = 1;
        }
    }

    public static Location getLastKnownLocation(Context context)
    {
        Location location = null;
        LocationManager locationmanager = (LocationManager)context.getSystemService("location");
        List list = locationmanager.getAllProviders();
        boolean i = false;
        Iterator iterator = list.iterator();
        do
        {
            //System.out.println("---------------------------------------------------------------------");
            if(!iterator.hasNext())
                break;
            String s = (String)iterator.next();
            //if(i != 0 && !locationmanager.isProviderEnabled(s))
            if(i != false && !locationmanager.isProviderEnabled(s))
                continue;
            // System.out.println("provider ===> "+s);
            Location location1 = locationmanager.getLastKnownLocation(s);
            if(location1 == null)
                continue;
            if(location != null)
            {
                //System.out.println("location ===> "+location);
                //System.out.println("location1 ===> "+location);
                float f = location.getAccuracy();
                float f1 = location1.getAccuracy();
                if(f >= f1)
                {
                    long l = location1.getTime();
                    long l1 = location.getTime();
                    if(l - l1 <= 600000L)
                        continue;
                }
            }
            location = location1;
            // System.out.println("location  out ===> "+location);
            //System.out.println("location1 out===> "+location);
            i = locationmanager.isProviderEnabled(s);
            // System.out.println("---------------------------------------------------------------------");
        } while(true);
        return location;
    }
}
