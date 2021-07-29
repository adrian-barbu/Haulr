package com.haulr.payment.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("success")
    private boolean isSuccess;
    public boolean isSuccess() { return isSuccess; }

    @SerializedName("msg")
    private String mMessage;
    public String getMessage() {
        return mMessage;
    }

    @SerializedName("transaction_id")
    private String mTransactionID;
    public String getTransactionID() {
        return mTransactionID;
    }
}
