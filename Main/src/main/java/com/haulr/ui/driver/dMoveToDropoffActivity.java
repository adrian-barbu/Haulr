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
 * @description     Driver-Side Move to dropoff activity
 *                  All informations will be provided from Intent
 *
 *                  When driver select "Start haulr meter", then call this.
 *
 * @author          Adrian
 */
public class dMoveToDropoffActivity extends dBaseActivity {

    // Variables
    private ServiceInfo mServiceInfo;

    GoogleDirection gd;     // Google Direction Controller
    Document mDoc;          // Google Direction Document

    // UI Members
    TextView toDropoffAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_moveto_dropoff);

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
        toDropoffAddress = (TextView) findViewById(R.id.tvDropOffAddress);
        toDropoffAddress.setText(mServiceInfo.getDropOffAddress());

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

        // Add Dropoff Location
        addDropOffLocation(mServiceInfo.getDropOffAddress(), mServiceInfo.getDropOffLatitude(), mServiceInfo.getDropOffLongitude());

        // Add Driver's Current Location
        MyLocation myLocation = MyLocation.getInstance(this);;
        if (myLocation.getLocation() == null) {
            showToast(R.string.error_gps_provide);
        }
        else {
            addMyLocation(myLocation.getLatitude(), myLocation.getLongitude());

            LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            // Add Route Between My Position And Dropoff Location
            gd = new GoogleDirection(this);
            gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                public void onResponse(String status, Document doc, GoogleDirection gd) {
                    mMap.addPolyline(gd.getPolyline(doc, 2, Color.BLUE));
                }
            });
            gd.request(myLatLng,
                    new LatLng(mServiceInfo.getDropOffLatitude(), mServiceInfo.getDropOffLongitude()),
                    GoogleDirection.MODE_DRIVING);

        }

        moveCameraWithPadding(150);
    }

    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.tvCancelHaul:         // Cancel Haul
                cancelHaul(mServiceInfo);
                break;

            case R.id.tvArrivedAtDropoff:    // Arrived At DropOff
                noticeStatus(mServiceInfo, ServiceStatus.ARRIVED_AT_DROPOFF);
                break;

            case R.id.layoutNavigateToDropOff:  // Navigation to Dropoff
                navigate(mServiceInfo.getDropOffLatitude(), mServiceInfo.getDropOffLongitude());
                break;

            case R.id.ivPhoneCall:          // Call Customer's Phone
                doCallPhone(mServiceInfo);
                break;
        }
    }
}