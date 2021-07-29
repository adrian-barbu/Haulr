package com.haulr.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haulr.R;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.ui.MainActivity;
import com.haulr.ui.OpBasedActivity;
import com.haulr.ui.customer.MonitoringHaulActivity;
import com.haulr.ui.driver.dArrivePickupActivity;
import com.haulr.ui.driver.dMoveToDropoffActivity;
import com.haulr.ui.driver.dMoveToPickupActivity;
import com.haulr.utils.CommonUtil;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

/**
 * @description     Pending Request View Activity
 * @author          Adrian
 */
public class PendingHaulActivity extends OpBasedActivity {

    // Variables
    private Service mService;
    private ServiceInfo mServiceInfo;

    // UI Members
    private TextView tvTitle;
    private ImageView ivPhoneCall;
    private TextView tvPickupLocation, tvDropOffLocation;
    private TextView tvPaymentMethod, tvPrice;
    private TextView tvStatus;

    private TextView tvCancel, tvComplete;

    private View layoutLoading, layoutContent, layoutStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_haul);

        layoutLoading = (View) findViewById(R.id.layoutLoading);
        layoutContent = (View) findViewById(R.id.layoutContent);
        layoutStatus = (View) findViewById(R.id.layoutStatus);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(isDriver ? R.string.pending_haul_driver_title : R.string.pending_haul_customer_title);

        ivPhoneCall = (ImageView) findViewById(R.id.ivPhoneCall);
        ivPhoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doCallPhone(mService);
            }
        });

        getDatas();
    }

    /**
     * Get Datas From Server
     */
    private void getDatas() {

        ParseQuery<Service> serviceQuery = ParseQuery.getQuery(Service.SERVICE_TABLE_NAME);
        serviceQuery.whereEqualTo(isDriver ? Service.FIELD_DRIVER : Service.FIELD_CUSTOMER, mCurrentUser);
        serviceQuery.whereNotEqualTo(Service.FIELD_STATUS, ServiceStatus.COMPLETED.toString());
        serviceQuery.getFirstInBackground(new GetCallback<Service>() {

            @Override
            public void done(Service service, ParseException e) {
                layoutLoading.setVisibility(View.INVISIBLE);
                layoutContent.setVisibility(View.VISIBLE);

                if (service != null) {
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
        tvPickupLocation = (TextView) findViewById(R.id.tvPickupLocation);
        tvPickupLocation.setText(String.format("%s %s", getString(R.string.pending_haul_pickup_location), mServiceInfo.getPickupAddress()));

        tvDropOffLocation = (TextView) findViewById(R.id.tvDropOffLocation);
        tvDropOffLocation.setText(String.format("%s %s", getString(R.string.pending_haul_dropoff_location), mServiceInfo.getDropOffAddress()));

        tvPaymentMethod = (TextView) findViewById(R.id.tvPaymentMethod);
        if (isDriver) {
            tvPaymentMethod.setVisibility(View.GONE);
            ((View)findViewById(R.id.layoutSeparatorForPayment)).setVisibility(View.GONE);
        } else {
            String cardNumber = mServiceInfo.getCardNumber();
            cardNumber = CommonUtil.hideCardNumber(cardNumber);
            tvPaymentMethod.setText(String.format("%s %s", getString(R.string.pending_haul_payment_method), cardNumber));
        }

        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvPrice.setText(String.format("%s%.2f", getString(R.string.pending_haul_price_of_haul), mServiceInfo.getPrice()));

        tvStatus = (TextView) findViewById(R.id.tvStatus);
        if (isDriver)
            layoutStatus.setVisibility(View.GONE);
        else
            tvStatus.setText(String.format("%s %s", getString(R.string.pending_haul_status), getStatusString(mService)));

        // Set buttons according to status
        tvCancel = (TextView) findViewById(R.id.tvCancel);
        tvComplete = (TextView) findViewById(R.id.tvComplete);

        ServiceStatus status = mService.getStatus();
        if (status == ServiceStatus.ARRIVED_AT_DROPOFF /* && !isDriver */) {
            tvCancel.setVisibility(View.GONE);
            tvComplete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.tvCancel:        // Cancel Haul
                noticeCancel(mService);
                break;

            case R.id.tvComplete:      // Complete Haul
                goCompleteActivity(mService.getObjectId());
                break;

            case R.id.tvContinue:      // Continue Haul
                continueHaul();
                break;

            case R.id.ivBack:
                finish();
                break;
        }
    }

    private void continueHaul() {
        if (isDriver) {
            continueToService(mService);
        }
        else {
            Intent intent = new Intent(this, MonitoringHaulActivity.class);
            intent.putExtra(PARAM_SERVICE_ID, mService.getObjectId());
            startActivity(intent);

            finish();
        }
    }

    /**
     * Continue to service for driver-side
     */
    private void continueToService(Service service) {
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

        if (intent != null) {
            intent.putExtra(PARAM_SERVICE_INFO, service.getServiceInfo());
            startActivity(intent);

            finish();
        }
    }
}