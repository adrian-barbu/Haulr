package com.haulr.payment;

public class Settings {

    //private static final String SANDBOX_BASE_SERVER_URL = "http://192.168.0.150/service2/payment";
    private static final String SANDBOX_BASE_SERVER_URL = "http://haulr.co/admin/payment";
    private static final String PRODUCTION_BASE_SERVER_URL = "https://executive-sample-merchant.herokuapp.com";

    // Urls
    private static final String PAYMENT_URL = "/nonce/transaction";     // Make Payment
    private static final String REFUND_URL = "/settlement_refund";      // Refund
    private static final String SUBMIT_URL = "/settlement_submit";      // Submit Payment
    private static final String VOID_URL = "/settlement_void";        // Void Payment

    // Other
    public static final boolean USE_SANDBOX = true;

    public static final boolean USE_NOTIFICATION = false;

    public static String getEnvironmentUrl() {
        if (USE_SANDBOX)
            return SANDBOX_BASE_SERVER_URL;
        else
            return PRODUCTION_BASE_SERVER_URL;
    }

    public static String getPaymentUrl() {
        return getEnvironmentUrl() + PAYMENT_URL;
    }

    public static String getRefundUrl() {
        return getEnvironmentUrl() + REFUND_URL;
    }

    public static String getSubmitUrl() {
        return getEnvironmentUrl() + SUBMIT_URL;
    }


    public static String getVoidUrl() {
        return getEnvironmentUrl() + VOID_URL;
    }


}
