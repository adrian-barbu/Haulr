package com.haulr.parse.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * @description     Service Info Object
 * @author          Adrian
 */
@ParseClassName("ServiceInfo")
public class ServiceInfo extends ParseObject implements Parcelable {
    // Table Name
    public final static String SERVICE_INFO_TABLE_NAME = "ServiceInfo";

    // Fields
    private final String FIELD_PICKUP_ADDRESS = "pickupAddress";
    private final String FIELD_PICKUP_LATITUDE = "pickupLatitude";
    private final String FIELD_PICKUP_LONGITUDE = "pickupLongitude";
    private final String FIELD_DROPOFF_ADDRESS = "dropoffAddress";
    private final String FIELD_DROPOFF_LATITUDE = "dropoffLatitude";
    private final String FIELD_DROPOFF_LONGITUDE = "dropoffLongitude";
    private final String FIELD_ITEM_IMAGE = "itemImage";
    private final String FIELD_DURATION = "duration";
    private final String FIELD_DISTANCE = "distance";
    private final String FIELD_PRICE = "price";
    private final String FIELD_TRANSACTION_ID = "transactionId";
    private final String FIELD_CARD_NUMBER = "cardNumber";

    private String mItemImageUrl;   // For Parcelable

    public ServiceInfo() {
        super();
    }

    public ServiceInfo(PlaceInfo pickupInfo, PlaceInfo dropoffInfo, String distance, String duration, double price, String cardNumber, String transactionID) {
        setPickupAddress(pickupInfo.getAddress());
        setPickupLatitude(pickupInfo.getLatitude());
        setPickupLongitude(pickupInfo.getLongitude());

        setDropoffAddress(dropoffInfo.getAddress());
        setDropoffLatitude(dropoffInfo.getLatitude());
        setDropoffLongitude(dropoffInfo.getLongitude());

        setDistance(distance);
        setDuration(duration);
        setPrice(price);

        setCardNumber(cardNumber);
        setTransactionID(transactionID);
    }

    // Getter
    public String getPickupAddress() {
        return getString(FIELD_PICKUP_ADDRESS);
    }
    public Double getPickupLatitude() {
        return getDouble(FIELD_PICKUP_LATITUDE);
    }
    public Double getPickupLongitude() {
        return getDouble(FIELD_PICKUP_LONGITUDE);
    }

    public String getDropOffAddress() {
        return getString(FIELD_DROPOFF_ADDRESS);
    }
    public Double getDropOffLatitude() {
        return getDouble(FIELD_DROPOFF_LATITUDE);
    }
    public Double getDropOffLongitude() {
        return getDouble(FIELD_DROPOFF_LONGITUDE);
    }

    public String getItemImageUrl() {
        if (mItemImageUrl != null)
            return mItemImageUrl;

        ParseFile file = getParseFile(FIELD_ITEM_IMAGE);
        if (file != null)
            return file.getUrl();
        else
            return null;
    }

    public String getDuration() {
        return getString(FIELD_DURATION);
    }
    public String getDistance() {
        return getString(FIELD_DISTANCE);
    }
    public double getPrice() {
        return getDouble(FIELD_PRICE);
    }
    public String getTransactionID() {
        return getString(FIELD_TRANSACTION_ID);
    }
    public String getCardNumber() {
        return getString(FIELD_CARD_NUMBER);
    }

    public ParseGeoPoint getPickupGeo() {
        try {
            fetchIfNeeded();
            return new ParseGeoPoint(getPickupLatitude(), getPickupLongitude());
        } catch (Exception ex) {
            return null;
        }
    }

    // Setter
    public void setPickupAddress(String value) {
        put(FIELD_PICKUP_ADDRESS, value);
    }
    public void setPickupLatitude(Double value) {
        put(FIELD_PICKUP_LATITUDE, value);
    }
    public void setPickupLongitude(Double value) {
        put(FIELD_PICKUP_LONGITUDE, value);
    }

    public void setDropoffAddress(String value) {
        put(FIELD_DROPOFF_ADDRESS, value);
    }
    public void setDropoffLatitude(Double value) {
        put(FIELD_DROPOFF_LATITUDE, value);
    }
    public void setDropoffLongitude(Double value) {
        put(FIELD_DROPOFF_LONGITUDE, value);
    }

    public void setDistance(String value) {
        put(FIELD_DISTANCE, value);
    }
    public void setDuration(String value) {
        put(FIELD_DURATION, value);
    }
    public void setPrice(double value) {
        put(FIELD_PRICE, value);
    }
    public void setCardNumber(String value) {
        put(FIELD_CARD_NUMBER, value.replace("-", ""));
    }
    public void setTransactionID(String value) {
        put(FIELD_TRANSACTION_ID, value);
    }

    public void setItemImage(ParseFile file) { put(FIELD_ITEM_IMAGE, file); }

    public void setItemImageUrl(String url) {
        mItemImageUrl = url;
    }

    ///////////////////////////////////////////////////////////////
    ///////////////////// Parcelable //////////////////////////////
    ///////////////////////////////////////////////////////////////

    public ServiceInfo(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        try {
            fetchIfNeeded();
        }
        catch (Exception e) {}

        dest.writeString(getObjectId());
        dest.writeString(getPickupAddress());
        dest.writeDouble(getPickupLatitude());
        dest.writeDouble(getPickupLongitude());
        dest.writeString(getDropOffAddress());
        dest.writeDouble(getDropOffLatitude());
        dest.writeDouble(getDropOffLongitude());
        dest.writeString(getItemImageUrl());
        dest.writeString(getDistance());
        dest.writeString(getDuration());
        dest.writeDouble(getPrice());
        dest.writeString(getTransactionID());
    }

    private void readFromParcel(Parcel in) {
        setObjectId(in.readString());
        setPickupAddress(in.readString());
        setPickupLatitude(in.readDouble());
        setPickupLongitude(in.readDouble());
        setDropoffAddress(in.readString());
        setDropoffLatitude(in.readDouble());
        setDropoffLongitude(in.readDouble());
        setItemImageUrl(in.readString());
        setDistance(in.readString());
        setDuration(in.readString());
        setPrice(in.readDouble());
        setTransactionID(in.readString());
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public ServiceInfo createFromParcel(Parcel in) {
                    return new ServiceInfo(in);
                }

                public ServiceInfo[] newArray(int size) {
                    return new ServiceInfo[size];
                }
            };
}
