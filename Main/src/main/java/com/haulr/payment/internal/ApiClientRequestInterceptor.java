package com.haulr.payment.internal;

import retrofit.RequestInterceptor;

public class ApiClientRequestInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("User-Agent", "haulr/1.0");
        //request.addHeader("Connection", "Keep-Alive");
        request.addHeader("Accept", "application/json");
    }
}
