package com.haulr.ui.driver;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.haulr.googleapi.GoogleDirection;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.ServiceStatus;

import org.w3c.dom.Document;

import com.haulr.R;

/**
 * @description     Driver-Side Arrive at Pickup activity
 *                  All informations will be provided from Intent
 *
 *                  When driver select "Accept haul", and click "Arrive at pickup", then call this.
 *                  When driver click "Start haulr meter" button, then start to move to drop-off location
 *
 * @author          Adrian
 */
public class dArrivePickupActivity extends dBaseActivity {

    // Variables
    private ServiceInfo mServiceInfo;

    GoogleDirection gd;     // Google Direction Controller
    Document mDoc;          // Google Direction Document

    // UI Members
    TextView tvPrice;
    TextView tvDropOffAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_arrive_pickup);

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
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvPrice.setText(String.format("%s $%.2f", getString(R.string.d_view_haul_title), mServiceInfo.getPrice()));

        tvDropOffAddress = (TextView) findViewById(R.id.tvDropOffAddress);
        tvDropOffAddress.setText(mServiceInfo.getDropOffAddress());

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

        // Add Dropoff Location
        addDropOffLocation(mServiceInfo.getDropOffAddress(), mServiceInfo.getDropOffLatitude(), mServiceInfo.getDropOffLongitude());

//        // Add Driver's Current Location
//        addLocation(getString(R.string.my_position),
//                locationVancover.latitude, locationVancover.longitude,
//                BitmapDescriptorFactory.HUE_BLUE);

        moveCameraWithPadding(150);

        // Add Route Between PickupLocation And DropOffLocation
        gd = new GoogleDirection(this);
        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                mDoc = doc;

                mMap.addPolyline(gd.getPolyline(doc, 4, Color.GREEN));
            }
        });
        gd.request(new LatLng(mServiceInfo.getPickupLatitude(), mServiceInfo.getPickupLongitude()),
                new LatLng(mServiceInfo.getDropOffLatitude(), mServiceInfo.getDropOffLongitude()),
                GoogleDirection.MODE_DRIVING);
    }

    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.tvStartHaulMeter:     // Start Haulr Meter
                noticeStatus(mServiceInfo, ServiceStatus.LEAVE_TO_DROPOFF);
                break;

            case R.id.ivPhoneCall:          // Call Customer's Phone
                doCallPhone(mServiceInfo);
                break;

            case R.id.ivBack: {
                finish();
                break;
            }
        }
    }
}