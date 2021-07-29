package com.haulr.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.googleapi.MyLocation;
import com.haulr.parse.ParseServiceEngine;
import com.haulr.parse.model.PlaceInfo;
import com.haulr.parse.ParseSession;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.parse.notification.NotificationManager;
import com.haulr.payment.Settings;
import com.haulr.ui.common.PendingHaulActivity;
import com.haulr.ui.setting.SettingActivity;
import com.haulr.ui.customer.PlaceSearchActivity;
import com.haulr.ui.customer.RequestHaulActivity;
import com.haulr.ui.driver.dChangeStatusActivity;
import com.haulr.ui.driver.dViewHaulActivity;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

/**
 * @description     Main Activity
 *                  Will be showed when both of driver and customer
 *
 * @author          Adrian
 */
public class MainActivity extends MapBasedActivity {

    /**
     * Request code passed to the PlacePicker intent to identify its result when it returns.
     */
    private static final int REQUEST_PICK_LOCATION = 1;
    private static final int REQUEST_HAUL_LOCATION = 2;
    private static final int REQUEST_SETTING = 3;

    private static final int REQUEST_CHANGE_STATUS = 4;    // When use customer layout

    // Variables
    private PlaceInfo mPickupLocation, mDropoffLocation;
    private boolean mHavePendingHaul;                       // The flag indicate whether pending job is exist or not
    private WaitDialog mWaitDialog;

    // UI Members
    private ProgressBar pgPending;
    private ImageView ivPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if current user act as a driver or not
        if (!mCurrentUser.asDriver())
            setContentView(R.layout.activity_main);
        else
            setContentView(R.layout.activity_d_main);

        pgPending = (ProgressBar) findViewById(R.id.pgPending);
        ivPending = (ImageView) findViewById(R.id.ivPending);

        mWaitDialog = new WaitDialog(this);

        initMapView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyLocation.getInstance(this);

        if (mCurrentUser.asDriver()) {
            changeStatus(mCurrentUser.isOnline());
        }

        // Find Pending Request
        doCheckHaul();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MyLocation.getInstance(this).stopListener();
    }

    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.warning_exit_app)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ParseSession.logOut(getApplicationContext());
                        MyLocation.getInstance(getApplicationContext()).stopListener();
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void setFirstMapView() {
        resetMap();

        if (mPickupLocation != null) {
            TextView tvPickUpAddress = (TextView) findViewById(R.id.tvPickUpAddress);
            tvPickUpAddress.setText(mPickupLocation.getAddress());
            addPickupLocation(mPickupLocation.getAddress(),
                    mPickupLocation.getLatitude(), mPickupLocation.getLongitude());
        }

        // Add Dropoff Location
        if (mDropoffLocation != null) {
            try {
                addDropOffLocation(mDropoffLocation.getAddress(),
                        mDropoffLocation.getLatitude(), mDropoffLocation.getLongitude());
            }
            catch (Exception e) {
            }
        }

        if (mPickupLocation == null && mDropoffLocation == null) {
            mBoundsBuilder.include(locationVancover);
            moveCameraWithZoom(13);
        }
        else if (mPickupLocation == null || mDropoffLocation == null) {
            moveCameraWithZoom(14);
        }
        else {
            moveCameraWithPadding(30);
        }
    }

    /**
     *  Item Click Listener
     *
     * @param v
     */
    public void onItemClick(View v) {
        switch (v.getId()) {
            case R.id.layoutPickupLocation:
                Intent intent = new Intent(this, PlaceSearchActivity.class);
                startActivityForResult(intent, REQUEST_PICK_LOCATION);
                break;

            case R.id.tvSetPickup:
                if (mHavePendingHaul)
                {
                    showToast(R.string.warning_not_request_haul);
                    return;
                }

                Intent intentHaulInfo = new Intent(this, RequestHaulActivity.class);
                if (mPickupLocation != null)
                    intentHaulInfo.putExtra(PARAM_PICKUP_LOCATION, mPickupLocation);
                startActivityForResult(intentHaulInfo, REQUEST_HAUL_LOCATION);
                break;

            case R.id.ivSetting:
                Intent intentSetting = new Intent(this, SettingActivity.class);
                startActivityForResult(intentSetting, REQUEST_SETTING);
                break;

            /**
             * In case of driver layout
             */
            case R.id.layoutCommand:
                if (mCurrentUser.isOnline()) {
                    if (mHavePendingHaul)
                        showToast(R.string.warning_not_acceptable_haul);
                    else
                        doSearchFreshHaulr();
                }
                break;

            case R.id.tvStatus:
                startActivityForResult(new Intent(this, dChangeStatusActivity.class), REQUEST_CHANGE_STATUS);
                break;

            case R.id.ivPending:
                startActivity(new Intent(this, PendingHaulActivity.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_LOCATION:     // Get Pickup Location
                if (resultCode == RESULT_OK) {
                    mPickupLocation = data.getParcelableExtra(PARAM_LOCATION);
                    setFirstMapView();
                }
                break;

            case REQUEST_HAUL_LOCATION:

                break;

            case REQUEST_CHANGE_STATUS:     // Get Pickup Location
                if (resultCode == RESULT_OK) {
                    boolean isOnline = data.getBooleanExtra(PARAM_DRIVER_STATUS, false);

                    boolean now = mCurrentUser.isOnline();
                    if (now != isOnline) {
                        // Change status on server
                        mCurrentUser.setOnline(isOnline);
                        mCurrentUser.saveInBackground();

                        changeStatus(isOnline);
                    }
                }
                break;
        }
    }

    /**
     * Change Status
     */
    private void changeStatus(boolean online) {
        TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
        TextView tvDynamicText = (TextView) findViewById(R.id.tvDynamicText);
        if (online) {
            tvStatus.setText(R.string.d_status_online);
            tvDynamicText.setText(R.string.d_scan_job);

            if (Settings.USE_NOTIFICATION) {
                // subscribe for notification
                if (mHavePendingHaul)   // if driver has pending job, they can accept new job.
                    showToast(R.string.warning_not_acceptable_haul);
                else
                    NotificationManager.getInstance().subscribe(this);
            }
        } else {
            tvStatus.setText(R.string.d_status_offline);
            tvDynamicText.setText(R.string.d_go_online);

            if (Settings.USE_NOTIFICATION) {
                // unsubscribe for notification
                NotificationManager.getInstance().unSubscribe();
            }
        }
    }

    /**
     * Check if one more of haul is exist
     */
    private void doCheckHaul() {
        // First, check if my uncompleted haul is exist or not
        pgPending.setVisibility(View.VISIBLE);
        ivPending.setVisibility(View.INVISIBLE);

        ParseQuery<Service> serviceQuery = ParseQuery.getQuery(Service.SERVICE_TABLE_NAME);
        serviceQuery.whereNotEqualTo(Service.FIELD_STATUS, ServiceStatus.COMPLETED.toString());
        serviceQuery.whereEqualTo(mCurrentUser.asDriver() ? Service.FIELD_DRIVER : Service.FIELD_CUSTOMER, mCurrentUser);
        serviceQuery.getFirstInBackground(new GetCallback<Service>() {
            @Override
            public void done(final Service service, ParseException e) {
                pgPending.setVisibility(View.GONE);

                if (service != null) {
                    ivPending.setVisibility(View.VISIBLE);
                    ivPending.setEnabled(true);

                    mHavePendingHaul = true;
                } else {
                    ivPending.setVisibility(View.INVISIBLE);
                    ivPending.setEnabled(false);

                    mHavePendingHaul = false;
                }
            }
        });
    }

    /**
     * Search new fresh haulr
     */
    private void doSearchFreshHaulr() {
        final MyLocation myLocation = MyLocation.getInstance(this);;
        if (myLocation.getLocation() == null) {
            showToast(R.string.error_gps_provide);
            return;
        }

        mWaitDialog.show();

        // Find new fresh haul
        ParseQuery<Service> serviceQuery = ParseQuery.getQuery(Service.SERVICE_TABLE_NAME);
        serviceQuery.whereNotEqualTo(Service.FIELD_CUSTOMER, mCurrentUser);     // Not allowed of my request
        serviceQuery.whereEqualTo(Service.FIELD_STATUS, ServiceStatus.PENDING.toString());
        serviceQuery.findInBackground(new FindCallback<Service>() {
            @Override
            public void done(List<Service> list, ParseException e) {
                if (list != null && list.size() > 0) {
                    ServiceInfo si = ParseServiceEngine.findNearestService(list, myLocation.getLocation());

                    if (si != null) {
                        Intent intentViewHaul = new Intent(MainActivity.this, dViewHaulActivity.class);
                        intentViewHaul.putExtra(PARAM_SERVICE_INFO, si);
                        startActivity(intentViewHaul);
                    } else {
                        showToast(R.string.d_no_haul);
                    }
                } else {
                    showToast(R.string.d_no_haul);
                }

                // There is not found matched
                mWaitDialog.dismiss();
                list = null;
                System.gc();
            }
        });
    }
}