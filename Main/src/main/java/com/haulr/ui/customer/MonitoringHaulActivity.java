package com.haulr.ui.customer;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.haulr.R;
import com.haulr.googleapi.GoogleDirection;
import com.haulr.googleapi.MyLocation;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.ui.OpBasedActivity;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.w3c.dom.Document;

/**
 * @description     Monitoring Haul Activity
 * @author          Adrian
 */
public class MonitoringHaulActivity extends OpBasedActivity {

    private final static int MONITORING_DELAY = 10 * 1000;   // 10 seconds

    // Variables
    private String mServiceId;
    private Service mService;

    private Handler mHandlerMonitoring;

    // UI Members
    TextView tvOpLeaveToPickup, tvOpArriveAtPickup;
    TextView tvOpLeaveToDropoff, tvOpArriveAtDropoff;
    ProgressBar pgUpdate;

    TextView tvCancel, tvComplete;

    boolean isFirstUpdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_haul);

        mServiceId = getIntent().getStringExtra(PARAM_SERVICE_ID);
//        mServiceId = "tGmuzSB1sP";

        initUI();
        getDatas();

        mHandlerMonitoring = new Handler();
        mHandlerMonitoring.removeCallbacksAndMessages(null);
        mHandlerMonitoring.postDelayed(mRunnableMonitoring, MONITORING_DELAY);
    }

    private void getDatas() {
        // Get Service From ServiceID
        ParseQuery<Service> serviceQuery = ParseQuery.getQuery(Service.SERVICE_TABLE_NAME);
        serviceQuery.getInBackground(mServiceId, new GetCallback<Service>() {
            @Override
            public void done(Service service, ParseException e) {
                pgUpdate.setVisibility(View.INVISIBLE);

                if (service != null) {
                    mService = service;
                    resetUI();

                    if (isFirstUpdate) {
                        initMapView();
                        isFirstUpdate = false;
                    }
                } else {
                    //showToast("Network is not availble. Please check it.");
                }
            }
        });
    }

    private void initUI() {
        tvOpLeaveToPickup = (TextView) findViewById(R.id.tvOpLeaveToPickup);
        tvOpArriveAtPickup = (TextView) findViewById(R.id.tvOpArriveAtPickup);
        tvOpLeaveToDropoff = (TextView) findViewById(R.id.tvOpLeaveToDropoff);
        tvOpArriveAtDropoff = (TextView) findViewById(R.id.tvOpArriveAtDropoff);
        pgUpdate = (ProgressBar) findViewById(R.id.pgUpdate);

        tvCancel = (TextView) findViewById(R.id.tvCancel);
        tvComplete = (TextView) findViewById(R.id.tvComplete);
    }

    /**
     * Reset ui When status is changed.
     */
    private void resetUI() {
        if (mService == null)
            return;

        ServiceStatus status = mService.getStatus();

        switch (status) {
            case LEAVE_TO_PICKUP:
                tvOpLeaveToPickup.setVisibility(View.VISIBLE);
                break;

            case ARRIVED_AT_PICKUP:
                tvOpLeaveToPickup.setVisibility(View.VISIBLE);
                tvOpArriveAtPickup.setVisibility(View.VISIBLE);
                break;

            case LEAVE_TO_DROPOFF:
                tvOpLeaveToPickup.setVisibility(View.VISIBLE);
                tvOpArriveAtPickup.setVisibility(View.VISIBLE);
                tvOpLeaveToDropoff.setVisibility(View.VISIBLE);
                break;

            case ARRIVED_AT_DROPOFF:
                tvOpLeaveToPickup.setVisibility(View.VISIBLE);
                tvOpArriveAtPickup.setVisibility(View.VISIBLE);
                tvOpLeaveToDropoff.setVisibility(View.VISIBLE);
                tvOpArriveAtDropoff.setVisibility(View.VISIBLE);
                tvCancel.setVisibility(View.GONE);
                tvComplete.setVisibility(View.VISIBLE);
                break;

            case PENDING:
            default:
                tvOpLeaveToPickup.setVisibility(View.GONE);
                tvOpLeaveToPickup.setVisibility(View.GONE);
                tvOpLeaveToPickup.setVisibility(View.GONE);
                tvOpLeaveToPickup.setVisibility(View.GONE);

                tvCancel.setVisibility(View.VISIBLE);
                tvComplete.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goMainActivity();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mHandlerMonitoring != null) {
            mHandlerMonitoring.removeCallbacksAndMessages(null);
            mHandlerMonitoring = null;
        }
    }

    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.ivBack:
                goMainActivity();
                break;

            case R.id.tvCancel:     // Cancel Operation
                noticeCancel(mService);
                break;

            case R.id.tvComplete:     // Complete Operation
                goCompleteActivity(mService.getObjectId());
                break;

            case R.id.layoutPhoneCall:  // Call Driver's Phone Number
                doCallPhone(mService);
                break;
        }
    }

    @Override
    public void setFirstMapView() {
        ServiceInfo serviceInfo = mService.getServiceInfo();
        try {
            serviceInfo.fetchIfNeeded();
            // Add Pickup Location
            addPickupLocation(serviceInfo.getPickupAddress(), serviceInfo.getPickupLatitude(), serviceInfo.getPickupLongitude());

            // Add Dropoff Location
            addDropOffLocation(serviceInfo.getDropOffAddress(), serviceInfo.getDropOffLatitude(), serviceInfo.getDropOffLongitude());

            moveCameraWithPadding(150);

            // Add Route Between Pickup And DropOff
            GoogleDirection gd = new GoogleDirection(this);
            gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                public void onResponse(String status, Document doc, GoogleDirection gd) {
                    mMap.addPolyline(gd.getPolyline(doc, 4, Color.GREEN));
                }
            });
            gd.request(new LatLng(serviceInfo.getPickupLatitude(), serviceInfo.getPickupLongitude()),
                    new LatLng(serviceInfo.getDropOffLatitude(), serviceInfo.getDropOffLongitude()),
                    GoogleDirection.MODE_DRIVING);
        } catch (Exception e) {}
    }

    /**
     * Runnable for monitoring status
     */
    private final Runnable mRunnableMonitoring = new Runnable() {
        @Override
        public void run() {
            mService = null;
            pgUpdate.setVisibility(View.VISIBLE);
            getDatas();
            mHandlerMonitoring.postDelayed(mRunnableMonitoring, MONITORING_DELAY);
        }
    };
}