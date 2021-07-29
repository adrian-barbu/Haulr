package com.haulr.ui.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.parse.ParseServiceEngine;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.parse.model.User;
import com.haulr.twilio.SendSMS;
import com.haulr.ui.MainActivity;
import com.haulr.ui.MapBasedActivity;
import com.haulr.ui.common.CompleteHaulActivity;
import com.haulr.utils.CommonUtil;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;

/**
 * @description     Driver-Side Base Activity
 *
 * @author          Adrian
 */
public class dBaseActivity extends MapBasedActivity {

    WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWaitDialog = new WaitDialog(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    /*************************************************/
    /***************  Map Functions  *****************/
    /*************************************************/

    @Override
    public void setFirstMapView() {}

    /**
     * Show navigation to pickup address from current location
     */
    protected void navigate(double latitude, double longitude) {
        Uri uri = Uri.parse(String.format("geo:%f,%f", latitude, longitude));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * Call Customer's Phone
     */
    protected void doCallPhone(ServiceInfo serviceInfo) {
        // if customer's phone number is exist, then call phone
        mWaitDialog.show();
        ParseServiceEngine.getService(serviceInfo, mGetServiceCallback);
    }

    /*************************************************/
    /*************  Service Functions  ***************/
    /*************************************************/

    /**
     * Cancel Haul
     *
     * When driver tapped up cancel haul, show warning dialog
     */
    protected void cancelHaul(final ServiceInfo serviceInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.warning_cancel_haul)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancel Operation
                        cancel(serviceInfo);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Cancel operation
     */
    private void cancel(ServiceInfo serviceInfo) {
        mWaitDialog.show();
        ParseServiceEngine.getService(serviceInfo, mCancelCallback);
    }

    private void failedToCancel(String message) {
        mWaitDialog.dismiss();
        showToast(R.string.error_cancel_haul);
    }

    /**
     * Cancel Callback
     */
    GetCallback<Service> mCancelCallback = new GetCallback<Service>() {
        @Override
        public void done(Service service, ParseException e) {
            if (service != null) {
                service.remove(Service.FIELD_DRIVER);   // Delete driver
                service.setStatus(ServiceStatus.PENDING); // Update status
                service.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            mWaitDialog.dismiss();
                            showToast(R.string.success_cancel_haul);
                            finish(); // Go to main
                        } else {
                            failedToCancel(e.getMessage());
                        }
                    }
                });

            } else {
                failedToCancel(e.getMessage());
            }
        }
    };


    /**
     * Get Service Callback
     */
    GetCallback<Service> mGetServiceCallback = new GetCallback<Service>() {
        @Override
        public void done(Service service, ParseException e) {
            if (service != null) {
                mWaitDialog.dismiss();

                String phoneNumber = "";
                try {
                    // Get customer's phone number
                    User customer = service.getCustomer();
                    customer.fetchIfNeeded();
                    phoneNumber = customer.getPhoneNumber();
                } catch (Exception ex)
                {
                }

                if (phoneNumber.isEmpty()) {
                    showToast(R.string.failed_get_customer_phone_number);
                    return;
                }

                // Call phone
                CommonUtil.callPhoneNumber(dBaseActivity.this, phoneNumber);
            } else {
                mWaitDialog.dismiss();
                showToast("Failed to get service information. Please retry!");
            }
        }
    };

    private ServiceStatus mStatus;
    private String mTwilioMessage;

    /**
     * Notice status to service
     *
     * When this, service will be sent the message via twilio to client.
     * Also, it will change its status to another
     */
    protected void noticeStatus(ServiceInfo serviceInfo, ServiceStatus status) {
        mWaitDialog.show();

        mStatus = status;
        switch (mStatus) {
            case ARRIVED_AT_PICKUP:
                mTwilioMessage = getString(R.string.d_arrive_pickup_message);
                break;

            case ARRIVED_AT_DROPOFF:
                mTwilioMessage = getString(R.string.d_arrive_dropoff_message);
                break;
        }

        ParseServiceEngine.getService(serviceInfo, mNoticeCallback);
    }

    /**
     * Notice Callback
     */
    GetCallback<Service> mNoticeCallback = new GetCallback<Service>() {
        @Override
        public void done(final Service service, ParseException e) {
            if (service != null) {
                service.setStatus(mStatus);     // Update status to New Status
                service.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {

                            if (mTwilioMessage != null && !mTwilioMessage.isEmpty()) {
                                // Send message via twilio to customer
                                User customer = service.getCustomer();
                                try {
                                    customer.fetchIfNeeded();
                                    String phoneNumber = customer.getPhoneNumber();
                                    SendSMS.sendMessage(customer.getPhoneNumber(), mTwilioMessage, mOnSendMessageListener);
                                } catch (Exception ex) {
                                    failedUpdateStatus();
                                }
                            }
                            else
                            {
                                goNextActivity();
                            }
                        } else {
                            failedUpdateStatus();
                        }
                    }
                });

            } else {
                failedUpdateStatus();
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

            goNextActivity();
        }

        @Override
        public void onError(String message) {
            mWaitDialog.dismiss();
            showToast(R.string.failed_send_message);

            goNextActivity();
        }
    };

    private void failedUpdateStatus() {
        mWaitDialog.dismiss();
        showToast(R.string.error_update_your_haul);
    }

    private void goNextActivity() {
        Intent intent = null;

        switch (mStatus) {
            case ARRIVED_AT_PICKUP:
                intent = new Intent(this, dArrivePickupActivity.class);
                break;

            case LEAVE_TO_DROPOFF:
                intent = new Intent(this, dMoveToDropoffActivity.class);
                break;

            case ARRIVED_AT_DROPOFF:
                intent = new Intent(this, CompleteHaulActivity.class);
                break;
        }

        if (intent != null) {
            intent.putExtras(getIntent());
            startActivity(intent);

            finish();
        }
    }


    /**
     * Continue to my uncompleted service
     *
     * @param service
     */
    protected void goMyService(Service service)
    {
        try {
            service.fetchIfNeeded();

            Intent intent = null;

            ServiceStatus status = service.getStatus();
            switch (status) {
                case LEAVE_TO_PICKUP:
                    intent = new Intent(this, dMoveToPickupActivity.class);
                    break;

                case ARRIVED_AT_PICKUP:
                    intent = new Intent(this, dArrivePickupActivity.class);
                    break;

                case LEAVE_TO_DROPOFF:
                    intent = new Intent(this, dMoveToDropoffActivity.class);
                    break;

                case ARRIVED_AT_DROPOFF:
                    intent = new Intent(this, CompleteHaulActivity.class);
                    break;
            }

            // Add other status

            // Go to intent
            if (intent != null) {
                ServiceInfo serviceInfo = service.getServiceInfo();
                intent.putExtra(PARAM_SERVICE_INFO, serviceInfo);
                startActivity(intent);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
