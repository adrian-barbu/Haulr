package com.haulr.ui.driver;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.googleapi.GoogleDirection;
import com.haulr.googleapi.MyLocation;
import com.haulr.parse.ParseServiceEngine;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.parse.model.User;
import com.haulr.twilio.SendSMS;
import com.haulr.ui.MainActivity;
import com.haulr.ui.MapBasedActivity;
import com.haulr.utils.ImageUtil;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;

import org.w3c.dom.Document;

/**
 * @description     View Haul on Driver-Side
 *                  All informations will be provided from Intent
 *
 * @author          Adrian
 */
public class dViewHaulActivity extends MapBasedActivity {

    // Variables
    private ServiceInfo mServiceInfo;

    GoogleDirection gd;     // Google Direction Controller
    GoogleDirection gd1;     // Google Direction Controller
    Document mDoc;          // Google Direction Document

    WaitDialog mWaitDialog;
    Animation mEnterAnim, mExitAnim;

    // UI Members
    TextView tvPrice;
    TextView tvPickUpLocation, tvDropOffLocation;
    TextView tvEstimation, tvEstimation1;
    ImageView ivPhoto;

    View layoutPickInfo, layoutMapBased;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_view_haul);

        mWaitDialog = new WaitDialog(this);
        mEnterAnim = AnimationUtils.loadAnimation(this, R.anim.enter_from_right_1);
        mEnterAnim.setAnimationListener(mEnterListener);

        mExitAnim = AnimationUtils.loadAnimation(this, R.anim.exit_to_right_1);
        mExitAnim.setAnimationListener(mExitListener);

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

        tvPickUpLocation = (TextView) findViewById(R.id.tvPickUpLocation);
        tvPickUpLocation.setText(mServiceInfo.getPickupAddress());

        tvDropOffLocation = (TextView) findViewById(R.id.tvDropOffLocation);
        tvDropOffLocation.setText(mServiceInfo.getDropOffAddress());

        tvEstimation = (TextView) findViewById(R.id.tvEstimation);
        String estimation = String.format("%s - %s", mServiceInfo.getDistance(), mServiceInfo.getDuration());
        tvEstimation.setText(estimation);

        tvEstimation1 = (TextView) findViewById(R.id.tvEstimation1);
        String estimation1 = String.format("%s distance - %s", mServiceInfo.getDistance(), mServiceInfo.getDuration());
        tvEstimation1.setText(estimation1);

        String photoUrl = mServiceInfo.getItemImageUrl();
        if (photoUrl != null) {
            ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
            ImageUtil.displayImage(ivPhoto, photoUrl, null);
        }

        layoutPickInfo = (View) findViewById(R.id.layoutPickInfo);
        layoutMapBased = (View) findViewById(R.id.layoutMapBased);

        initMapView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mServiceInfo = null;
    }

    @Override
    public void setFirstMapView() {
        // Add Pickup Location
        addPickupLocation(mServiceInfo.getPickupAddress(), mServiceInfo.getPickupLatitude(), mServiceInfo.getPickupLongitude());

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

            // Add Route Between CurrentPosition And CurrentPosition
            gd1 = new GoogleDirection(this);
            gd1.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                public void onResponse(String status, Document doc, GoogleDirection gd) {
                    mMap.addPolyline(gd.getPolyline(doc, 2, Color.BLUE));
                }
            });
            gd1.request(myLatLng,
                    new LatLng(mServiceInfo.getPickupLatitude(), mServiceInfo.getPickupLongitude()),
                    GoogleDirection.MODE_DRIVING);
        }

        moveCameraWithPadding(150);

        // Add Route Between Pickup And DropOff
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
            case R.id.layoutEstimation:     // View on Map
                layoutMapBased.startAnimation(mEnterAnim);
                break;

            case R.id.layoutEstimation1:    // View on List
                layoutMapBased.startAnimation(mExitAnim);
                break;

            case R.id.tvAcceptHaul:         // Accept Haul
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
        ParseServiceEngine.getService(mServiceInfo, mAcceptCallback);
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
                        startActivity(new Intent(dViewHaulActivity.this, MainActivity.class));
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Accept Callback
     */
    GetCallback<Service> mAcceptCallback = new GetCallback<Service>() {
        @Override
        public void done(final Service service, ParseException e) {
            if (service != null) {
                // Check if another driver is accepted already.
                if (service.getStatus() != ServiceStatus.PENDING)
                {
                    doActionByAlreadyStarted();
                    return;
                }

                // Start to accept
                service.setDriver(mCurrentUser);    // Register me as a driver
                service.setStatus(ServiceStatus.LEAVE_TO_PICKUP);   // Update status
                service.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            mWaitDialog.dismiss();

                            // Send message via twilio to customer
                            User customer = service.getCustomer();
                            try {
                                customer.fetchIfNeeded();
                                SendSMS.sendMessage(customer.getPhoneNumber(), getString(R.string.d_accept_request), mOnSendMessageListener);
                            } catch (Exception ex) {
                            }

                            // Go to next step
                            Intent intentViewHaul = new Intent(dViewHaulActivity.this, dMoveToPickupActivity.class);
                            intentViewHaul.putExtra(PARAM_SERVICE_INFO, mServiceInfo);
                            startActivity(intentViewHaul);

                            finish();
                        } else {
                            failedToAccept(e.getMessage());
                        }
                    }
                });

            } else {
                failedToAccept(e.getMessage());
            }
        }
    };

    /**
     * Sent Message Listener
     *
     * This will be used when driver try to send message to customer
     */
    SendSMS.OnSendMessageListener mOnSendMessageListener = new SendSMS.OnSendMessageListener() {
        @Override
        public void onMessageSent(String message) {
            mWaitDialog.dismiss();
            showToast(R.string.success_send_message);
        }

        @Override
        public void onError(String message) {
            mWaitDialog.dismiss();
            showToast(R.string.failed_send_message);
        }
    };

    /**
     * Animations (Enter)
     */
    private Animation.AnimationListener mEnterListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            layoutMapBased.setVisibility(View.VISIBLE);
            layoutPickInfo.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };

    /**
     * Animations (Exit)
     */
    private Animation.AnimationListener mExitListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            layoutMapBased.setVisibility(View.INVISIBLE);
            layoutPickInfo.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };
}