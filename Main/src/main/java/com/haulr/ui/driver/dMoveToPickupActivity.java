package com.haulr.ui.driver;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.haulr.googleapi.GoogleDirection;
import com.haulr.googleapi.MyLocation;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.ServiceStatus;

import org.w3c.dom.Document;

import com.haulr.R;

/**
 * @description     Driver-Side Move to pickup activity
 *                  All informations will be provided from Intent
 *
 *                  When driver select "Accept haul", then call this, so that driver can start the haul.
 *
 * @author          Adrian
 */
public class dMoveToPickupActivity extends dBaseActivity {

    // Variables
    private ServiceInfo mServiceInfo;

    GoogleDirection gd;     // Google Direction Controller
    Document mDoc;          // Google Direction Document

    // UI Members
    TextView toPickupAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_moveto_pickup);

        getParams();
    }

    private void getParams() {
        mServiceInfo = getIntent().getParcelableExtra(PARAM_SERVICE_INFO);
        if (mServiceInfo != null)
            initUI();
        else
            finish();
    }

    private void initUI() {
        toPickupAddress = (TextView) findViewById(R.id.toPickupAddress);
        toPickupAddress.setText(mServiceInfo.getPickupAddress());

        initMapView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mServiceInfo = null;
    }

    @Override
    public void setFirstMapView() {
        super.setFirstMapView();

        // Add Pickup Location
        addPickupLocation(mServiceInfo.getPickupAddress(), mServiceInfo.getPickupLatitude(), mServiceInfo.getPickupLongitude());

        // Add Driver's Current Location
        MyLocation myLocation = MyLocation.getInstance(this);;
        if (myLocation.getLocation() == null) {
            showToast(R.string.error_gps_provide);
        }
        else {
            addMyLocation(myLocation.getLatitude(), myLocation.getLongitude());

            LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            // Add Route Between My Position And Pickup Location
            gd = new GoogleDirection(this);
            gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                public void onResponse(String status, Document doc, GoogleDirection gd) {
                    mMap.addPolyline(gd.getPolyline(doc, 2, Color.BLUE));
                }
            });
            gd.request(myLatLng,
                    new LatLng(mServiceInfo.getPickupLatitude(), mServiceInfo.getPickupLongitude()),
                    GoogleDirection.MODE_DRIVING);
        }

        moveCameraWithPadding(150);
    }

    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.tvCancelHaul:         // Cancel Haul
                cancelHaul(mServiceInfo);
                break;

            case R.id.tvArrivedAtPickup:    // Arrived At Pickup
                noticeStatus(mServiceInfo, ServiceStatus.ARRIVED_AT_PICKUP);
                break;

            case R.id.layoutNavigateToPickup:   // Navigato to Pickup Adress
                navigate(mServiceInfo.getPickupLatitude(), mServiceInfo.getPickupLongitude());
                break;

            case R.id.ivBack: {
                finish();
                break;
            }
        }
    }
}