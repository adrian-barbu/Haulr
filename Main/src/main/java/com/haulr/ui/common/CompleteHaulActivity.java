package com.haulr.ui.common;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.haulr.R;
import com.haulr.parse.ParseServiceEngine;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.ui.OpBasedActivity;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

/**
 * @description     Complete Haul Activity
 * @author          Adrian
 */
public class CompleteHaulActivity extends OpBasedActivity {

    // Variables
    private String mServiceId;
    private Service mService;
    private ServiceInfo mServiceInfo;

    // UI Members
    private TextView tvPrice, tvDistanceTime;
    private TextView tvRateExperience;
    private RatingBar ratingBar;

    private View layoutLoading, layoutContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_haul);

        if (getIntent().hasExtra(PARAM_SERVICE_INFO))
        {
            mServiceInfo = getIntent().getParcelableExtra(PARAM_SERVICE_INFO);
            getDatasByServiceInfo(mServiceInfo);
        }
        else {
            mServiceId = getIntent().getStringExtra(PARAM_SERVICE_ID);
            getDatas();
        }
    }

    private void getDatas() {
        // Get Service From ServiceID
        ParseQuery<Service> serviceQuery = ParseQuery.getQuery(Service.SERVICE_TABLE_NAME);
        serviceQuery.getInBackground(mServiceId, new GetCallback<Service>() {
            @Override
            public void done(Service service, ParseException e) {
                if (service != null) {
                    mService = service;
                    try {
                        mServiceInfo = mService.getServiceInfo();
                        mServiceInfo.fetchIfNeeded();
                    } catch (Exception ex) {
                    }

                    initUI();
                } else {
                    showToast("Network is not availble. Please check it.");
                }
            }
        });
    }

    private void getDatasByServiceInfo(ServiceInfo si) {
        ParseServiceEngine.getService(si, new GetCallback<Service>() {
            @Override
            public void done(Service service, ParseException e) {
                if (service != null) {
                    mService = service;
                    mServiceId = service.getObjectId();
                    try {
                        mServiceInfo = mService.getServiceInfo();
                        mServiceInfo.fetchIfNeeded();
                    } catch (Exception ex) {
                    }

                    initUI();
                } else {
                    showToast("Network is not availble. Please check it.");
                }
            }
        });
    }

    private void initUI() {
        layoutLoading = (View) findViewById(R.id.layoutLoading);
        layoutLoading.setVisibility(View.GONE);

        layoutContent = (View) findViewById(R.id.layoutContent);
        layoutContent.setVisibility(View.VISIBLE);

        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvPrice.setText(String.format("$%.2f", mServiceInfo.getPrice()));

        tvDistanceTime = (TextView) findViewById(R.id.tvDistanceTime);
        tvDistanceTime.setText(String.format("%s - %s", mServiceInfo.getDistance(), mServiceInfo.getDuration()));

        tvRateExperience = (TextView) findViewById(R.id.tvRateExperience);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        if (isDriver) {
            ratingBar.setVisibility(View.INVISIBLE);
            tvRateExperience.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.tvCompleteHaul:        // Complete Haul
                noticeComplete(mService, ratingBar.getRating());
                break;

            case R.id.ivBack:
                goMainActivity();
                break;
        }
    }

}