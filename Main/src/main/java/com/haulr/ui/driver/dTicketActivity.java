package com.haulr.ui.driver;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.haulr.R;
import com.haulr.control.DowncountView;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.googleapi.GoogleDirection;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.ui.MainActivity;
import com.haulr.ui.MapBasedActivity;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.w3c.dom.Document;

/**
 * @description     Ticket on Driver-Side
 *                  All informations will be provided from Intent
 *
 * @author          Adrian
 */
public class dTicketActivity extends MapBasedActivity {

    // Intent Params
    public final static String PARAM_SERVICE_ID = "ServiceId";

    // Variables
    private String mServiceId;

    private Service mService;
    private ServiceInfo mServiceInfo;

    GoogleDirection gd;     // Google Direction Controller

    WaitDialog mWaitDialog;

    // UI Members
    TextView tvPickUpLocation, tvEstimation;
    View layoutLoading, layoutContent;
    DowncountView dvDowncounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_ticket);

        mServiceId = getIntent().getStringExtra(PARAM_SERVICE_ID);
        //mServiceId = "IAq6ytlwpa";
        mWaitDialog = new WaitDialog(this);

        layoutLoading = (View) findViewById(R.id.layoutLoading);
        layoutContent = (View) findViewById(R.id.layoutContent);

        getDatas();
    }

    /**
     * Get Datas From Server
     */
    private void getDatas() {
        ParseQuery<Service> serviceQuery = ParseQuery.getQuery(Service.SERVICE_TABLE_NAME);
        serviceQuery.getInBackground(mServiceId, new GetCallback<Service>() {
            @Override
            public void done(Service service, ParseException e) {
                if (service != null) {
                    layoutLoading.setVisibility(View.INVISIBLE);
                    layoutContent.setVisibility(View.VISIBLE);

                    mService = service;
                    mServiceInfo = service.getServiceInfo();
                    try {
                        mServiceInfo.fetchIfNeeded();
                        initUI();
                    } catch (Exception ex) {
                    }
                } else {
                    showToast("Unable to get service.");
                    return;
                }
            }
        });
    }

    private void initUI() {
        tvPickUpLocation = (TextView) findViewById(R.id.tvPickUpLocation);
        tvPickUpLocation.setText(mServiceInfo.getPickupAddress());

        tvEstimation = (TextView) findViewById(R.id.tvEstimation);
        String estimation = String.format("%s - %s", mServiceInfo.getDistance(), mServiceInfo.getDuration());
        tvEstimation.setText(estimation);

        dvDowncounter = (DowncountView) findViewById(R.id.dvDowncounter);

        initMapView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mService = null;
        mServiceInfo = null;

        System.gc();
    }

    @Override
    public void setFirstMapView() {

        // Add Pickup Location
        addPickupLocation(mServiceInfo.getPickupAddress(), mServiceInfo.getPickupLatitude(), mServiceInfo.getPickupLongitude());

        // Add Dropoff Location
        addDropOffLocation(mServiceInfo.getDropOffAddress(), mServiceInfo.getDropOffLatitude(), mServiceInfo.getDropOffLongitude());

        // Add Route Between Pickup And DropOff
        gd = new GoogleDirection(this);
        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                mMap.addPolyline(gd.getPolyline(doc, 4, Color.GREEN));
            }
        });
        gd.request(new LatLng(mServiceInfo.getPickupLatitude(), mServiceInfo.getPickupLongitude()),
                new LatLng(mServiceInfo.getDropOffLatitude(), mServiceInfo.getDropOffLongitude()),
                GoogleDirection.MODE_DRIVING);

        moveCameraWithPadding(150);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dvDowncounter.start();
            }
        }, 2000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    /**
     * Item Click Listener
     *
     * @param view
     */
    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.tvAcceptHaul:
                doAccept();
                break;

            case R.id.ivBack:
                finish();
                break;
        }
    }

    /**
     * Do Accept Haul Action
     */
    private void doAccept() {
        // Check if another driver is accepted already.
        if (mService.getStatus() != ServiceStatus.PENDING)
        {
            doActionByAlreadyStarted();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.confirm_accept_haul)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Accept Haul
                        accept();
                    }
                })
                .setNegativeButton(R.string.dialog_reject, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Accept operation
     */
    private void accept() {
        mWaitDialog.show();

        // Start to accept
        mService.setDriver(mCurrentUser);    // Register me as a driver
        mService.setStatus(ServiceStatus.LEAVE_TO_PICKUP);   // Update status
        mService.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mWaitDialog.dismiss();

                    // Go to next step
                    Intent intentViewHaul = new Intent(dTicketActivity.this, dMoveToPickupActivity.class);
                    intentViewHaul.putExtra(PARAM_SERVICE_INFO, mServiceInfo);
                    startActivity(intentViewHaul);

                    finish();
                } else {
                    failedToAccept(e.getMessage());
                }
            }
        });
    }

    private void failedToAccept(String message) {
        mWaitDialog.dismiss();
        showToast(R.string.error_accept_haul);
    }

    /**
     * The action when the haulr is already started by another driver
     */
    private void doActionByAlreadyStarted() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.error_already_accepted_by_another)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Go to Main Activity
                        startActivity(new Intent(dTicketActivity.this, MainActivity.class));
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}