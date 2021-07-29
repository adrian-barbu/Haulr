package com.haulr.parse.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @description     Defines Place Location Params
 * @author          Adrian
 */
public class PlaceInfo implements Parcelable {
    // Variables
    private String mName, mAddress;
    private double mLatitude, mLongitude;

    public PlaceInfo(String name, String address, double latitude, double longitude) {
        mName = name;
        mAddress = address;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public PlaceInfo(Parcel in) {
        readFromParcel(in);
    }

    // Getter / Setter
    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(mName);
        dest.writeString(mAddress);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
    }

    private void readFromParcel(Parcel in) {
        mName = in.readString();
        mAddress = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public PlaceInfo createFromParcel(Parcel in) {
                    return new PlaceInfo(in);
                }

                public PlaceInfo[] newArray(int size) {
                    return new PlaceInfo[size];
                }
            };
}
