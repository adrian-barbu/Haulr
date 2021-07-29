package com.haulr.parse.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * @description     Parse CreditCard Object
 * @author          Adrian
 */
@ParseClassName("CreditCard")
public class CreditCard extends ParseObject implements Parcelable {
    // Table Name
    public final static String CREDITCARD_TABLE_NAME = "CreditCard";

    // Fields
    public final static String FIELD_USER_ID = "userId";
    private final String FIELD_CARD_NUMBER = "cardNumber";
    private final String FIELD_EXPIRE_YEAR = "expireYear";
    private final String FIELD_EXPIRE_MONTH = "expireMonth";
    private final String FIELD_CVC = "CVC";

    public CreditCard() {
        super();
    }

    public CreditCard(String userId, String cardNumber, String expireYear, String expireMonth, String CVC) {
        setUserID(userId);
        setCardNumber(cardNumber);
        setExpireYear(expireYear);
        setExpireMonth(expireMonth);
        setCVC(CVC);
    }

    // Methods
    public String getUserID() {
        return getString(FIELD_USER_ID);
    }
    public String getCardNumber() {
        return getString(FIELD_CARD_NUMBER);
    }
    public String getExpireDate() { return String.format("%s/%s", getExpireMonth(), getExpireYear()); }
    public String getExpireYear() {
        return getString(FIELD_EXPIRE_YEAR);
    }
    public String getExpireMonth() {
        return getString(FIELD_EXPIRE_MONTH);
    }
    public String getCVC() {
        return getString(FIELD_CVC);
    }

    public void setUserID(String value) {
        put(FIELD_USER_ID, value);
    }
    public void setCardNumber(String value) {
        put(FIELD_CARD_NUMBER, value);
    }
    public void setExpireYear(String value) {
        put(FIELD_EXPIRE_YEAR, value);
    }
    public void setExpireMonth(String value) {
        put(FIELD_EXPIRE_MONTH, value);
    }
    public void setCVC(String value) {
        put(FIELD_CVC, value);
    }

    ///////////////////////////////////////////////////////////////
    ///////////////////// Parcelable //////////////////////////////
    ///////////////////////////////////////////////////////////////

    public CreditCard(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(getUserID());
        dest.writeString(getCardNumber());
        dest.writeString(getExpireYear());
        dest.writeString(getExpireMonth());
        dest.writeString(getCVC());
    }

    private void readFromParcel(Parcel in) {
        setUserID(in.readString());
        setCardNumber(in.readString());
        setExpireYear(in.readString());
        setExpireMonth(in.readString());
        setCVC(in.readString());
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public CreditCard createFromParcel(Parcel in) {
                    return new CreditCard(in);
                }

                public CreditCard[] newArray(int size) {
                    return new CreditCard[size];
                }
            };
}
