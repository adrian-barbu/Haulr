package com.haulr.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.parse.model.User;
import com.haulr.payment.Settings;
import com.haulr.twilio.SendSMS;
import com.haulr.ui.common.CompleteHaulActivity;
import com.haulr.utils.CommonUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * @description     Operation Based Activity
 * @author          Adrian
 */
public class OpBasedActivity extends MapBasedActivity {

    // Variables
    protected boolean isDriver;
    protected WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWaitDialog = new WaitDialog(this);
        isDriver = mCurrentUser.asDriver();
    }

    /**
     * Call Phone
     */
    protected void doCallPhone(Service service) {
        // if driver's phone number is exist, then call phone
        String phoneNumber = "";

        try {
            if (isDriver) {
                // Get customer's phone number
                User customer = service.getCustomer();
                customer.fetchIfNeeded();
                phoneNumber = customer.getPhoneNumber();
            }
            else {
                // Get driver's phone number
                User driver = service.getDriver();
                if (driver == null) {
                    showToast(R.string.status_pending);
                    return;
                }

                driver.fetchIfNeeded();
                phoneNumber = driver.getPhoneNumber();
            }
        } catch (Exception e)
        {
        }

        if (phoneNumber.isEmpty()) {
            showToast(isDriver ? R.string.failed_get_customer_phone_number : R.string.failed_get_driver_phone_number);
            return;
        }

        // Call phone
        CommonUtil.callPhoneNumber(this, phoneNumber);
    }

    /**
     * Notice to cancel
     *
     * If service haul was passed haul meter step, then not to cancel
     */
    protected void noticeCancel(final Service service) {
        // Check status
        ServiceStatus status = service.getStatus();
        boolean availableCancel = (status != ServiceStatus.LEAVE_TO_DROPOFF) &&
                                  (status != ServiceStatus.ARRIVED_AT_DROPOFF);

        if (!availableCancel) {
            showToast(R.string.error_no_cancel_haul);
            return;
        }

        // Popup warning dialog
        String warningMessage = (isDriver) ? getString(R.string.warning_cancel_haul) :
                                             getString(R.string.warning_customer_cancel_haul);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(warningMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (isDriver)
                            doCancelOperationForDriver(service);
                        else
                            doCancelOperation(service);
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
     * Start Cancel Operation (For Customer)
     *
     * There is difference between customer and driver
     */
    private void doCancelOperation(final Service service) {
        // Delete service from server
        mWaitDialog.show();

        final ServiceInfo serviceInfo = service.getServiceInfo();
        final String transactionId;

        try {
            serviceInfo.fetchIfNeeded();
            transactionId = serviceInfo.getTransactionID();
        } catch (Exception e)
        {
            failedToCancel();
            return;
        }

        // Delete Service
        serviceInfo.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    service.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            mWaitDialog.dismiss();
                            showToast((e == null) ? R.string.success_cancel_haul : R.string.error_cancel_haul);

                            doVoidPayment(transactionId);

                            // Go Main Activity
                            goMainActivity();
                        }
                    });
                } else {
                    failedToCancel();
                }
            }
        });


    }

    private void doVoidPayment(String transactionId) {
        // Void payment from Braintree
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("transaction_id", transactionId);
        //params.put("amount", mPrice);
        client.post(Settings.getVoidUrl(), params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        mWaitDialog.dismiss();

                        // Parse Result
                        try {
                            String response = new String(responseBody);

                            JSONObject object = new JSONObject(response);
                            boolean success = object.getBoolean("success");

                            mWaitDialog.dismiss();
                        } catch (Exception e) {
                            mWaitDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        mWaitDialog.dismiss();
                    }
                }
        );
    }

    /**
     * Start Cancel Operation (For Driver)
     *
     * There is difference between customer and driver
     */
    private void doCancelOperationForDriver(final Service service) {
        // Delete service from server
        mWaitDialog.show();

        service.remove(Service.FIELD_DRIVER);   // Delete driver
        service.setStatus(ServiceStatus.PENDING); // Update status
        service.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mWaitDialog.dismiss();
                    showToast(R.string.success_cancel_haul);
                    goMainActivity();
                } else {
                    failedToCancel();
                }
            }
        });
    }

    private void failedToCancel() {
        mWaitDialog.dismiss();
        showToast(R.string.error_cancel_haul);
    }

    /**
     * Notice complete
     *
     * Attetion: driver can't do action of complete, only for customer
     *
     * When this, service will be sent the message via twilio to driver.
     * Also, it will change its status to COMPLETE
     */
    protected void noticeComplete(final Service service, final float rating) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.warning_complete_haulr)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        doComplete(service, rating);
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

    private void doComplete(final Service service, final float rating) {
        mWaitDialog.show();
        service.setStatus(ServiceStatus.COMPLETED); // Update status to "completed"
        service.setSetRating(rating);               // Set Rating
        service.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // Do submit payment

                    try {
                        ServiceInfo serviceInfo = service.getServiceInfo();
                        serviceInfo.fetchIfNeeded();

                        User customer = service.getCustomer();
                        customer.fetchIfNeeded();

                        // Submit Payment
                        doSubmitPayment(serviceInfo.getTransactionID(), customer);
                    } catch (Exception ex) {
                        mWaitDialog.dismiss();
                        //showToast(R.string.failed_send_message);
                    }

                    // Go Main Activity
                    goMainActivity();
                } else {
                    mWaitDialog.dismiss();
                    showToast(R.string.error_update_your_haul);
                }
            }
        });
    }

    /**
     * Do Submit Payment
     */
    private void doSubmitPayment(String transactionId, final User customer) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("transaction_id", transactionId);
        client.post(Settings.getSubmitUrl(), params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        mWaitDialog.dismiss();

                        // Parse Result
                        try {
                            String response = new String(responseBody);

                            JSONObject object = new JSONObject(response);
                            boolean success = object.getBoolean("success");

                            mWaitDialog.dismiss();

                            if (success) {
                                // Send message via twilio to customer
                                SendSMS.sendMessage(customer.getPhoneNumber(), getString(R.string.d_complete_message), null);
                            } else {
                                // Fail
                                //showToast(R.string.error_pay_with_credit_card);
                            }
                        }
                        catch (Exception e)
                        {
                            //showToast(R.string.error_pay_with_credit_card);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        mWaitDialog.dismiss();
                        //showToast(R.string.error_create_transaction);
                    }
                }
        );
    }

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
            //showToast(R.string.failed_send_message);
        }
    };

    protected void goMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }

    protected void goCompleteActivity(String serviceId) {
        Intent intent = new Intent(this, CompleteHaulActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PARAM_SERVICE_ID, serviceId);
        startActivity(intent);

        finish();
    }

    /**
     * Get Status Full String From Status
     *
     * @return
     */
    protected String getStatusString(Service service) {
        String strStatus = "";

        ServiceStatus status = service.getStatus();
        switch (status) {
            case PENDING:
                strStatus = getString(R.string.status_pending);
                break;

            case LEAVE_TO_PICKUP:
                strStatus = getString(R.string.status_leave_to_pickup);
                break;

            case ARRIVED_AT_PICKUP:
                strStatus = getString(R.string.status_arrive_at_pickup);
                break;

            case LEAVE_TO_DROPOFF:
                strStatus = getString(R.string.status_leave_to_dropoff);
                break;

            case ARRIVED_AT_DROPOFF:
                strStatus = getString(R.string.status_arrive_at_dropoff);
                break;
        }

        return strStatus;
    }

    @Override
    public void setFirstMapView() {
    }
}