package com.haulr.ui.customer;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.googleapi.GoogleDirection;
import com.haulr.parse.model.PlaceInfo;
import com.haulr.control.photo.ChoosePhoto;
import com.haulr.payment.Settings;
import com.haulr.payment.internal.ApiClient;
import com.haulr.payment.internal.ApiClientRequestInterceptor;
import com.haulr.payment.models.ClientToken;
import com.haulr.ui.BaseActivity;
import com.haulr.utils.HaulrUtil;

import org.w3c.dom.Document;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @description     Set Haul Info Activity
 * @author           Adrian
 */
public class RequestHaulActivity extends BaseActivity {

    /**
     * Request code passed to the PlacePicker intent to identify its result when it returns.
     */
    private static final int REQUEST_PICKUP_LOCATION = 1;
    private static final int REQUEST_DROPOFF_LOCATION = 2;
    public final static int REQUEST_CHOOSE_PHOTO = 3;

    // Variables
    private ChoosePhoto mPhotoChooser;
    private String mItemImagePath;

    // Haul Information
    private PlaceInfo mPickupInfo, mDropOffInfo;
    private double mPrice;
    private String mDistance, mDuration;

    GoogleDirection gd;
    WaitDialog mWaitDialog;

    // UI Members
    private TextView tvPickUpLocation, tvDropOffLocation;
    private TextView tvPrice;
    private ImageView ivPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_haul);

        mPickupInfo = getIntent().getParcelableExtra(PARAM_PICKUP_LOCATION);    // PickUP Location From MainActivity

        mPhotoChooser = new ChoosePhoto(this);
        gd = new GoogleDirection(this);
        mWaitDialog = new WaitDialog(this);

        initUI();
    }

    private void initUI() {
        ivPhoto = (ImageView)findViewById(R.id.ivPhoto);

        // Set Pickup Information
        tvPickUpLocation = (TextView) findViewById(R.id.tvPickUpLocation);
        if (mPickupInfo != null) {
            String address = mPickupInfo.getAddress();
            if (address != null) {
                tvPickUpLocation.setText(address);
            }
        }

        tvDropOffLocation = (TextView) findViewById(R.id.tvDropOffLocation);

        tvPrice = (TextView) findViewById(R.id.tvPrice);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Delete photo bitmap
        if (mPhotoChooser != null) {
            mPhotoChooser = null;
        }

        System.gc();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.layoutPickPhoto:      // Pick Photo
                onPickPhoto();
                break;

            case R.id.layoutPickupLocation: // Pickup Location
                startActivityForResult(new Intent(this, PlaceSearchActivity.class), REQUEST_PICKUP_LOCATION);
                break;

            case R.id.layoutDropoffLocation: // Dropoff Location
                startActivityForResult(new Intent(this, PlaceSearchActivity.class), REQUEST_DROPOFF_LOCATION);
                break;

            case R.id.layoutPaymentMethod:  // Payment Method
                break;

            case R.id.tvRequestHaul:        // Request Service
                onRequestHaul();
                break;

            case R.id.tvCancel:
                finish();
                break;
        }
    }

    /**
     * Pick Photo
     */
    private void onPickPhoto() {
        final String takeFromCamera = getString(R.string.take_from_camera);
        final String takeFromGallery = getString(R.string.take_from_gallery);
        final String cancel = getString(R.string.dialog_cancel);

        final String[] items = { takeFromCamera, takeFromGallery, cancel };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog);
        builder.setTitle(R.string.photo_picker);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(takeFromCamera)) {
                    // take photo from camera
                    mPhotoChooser.dispatchTakePictureIntent(ChoosePhoto.SHOT_IMAGE, REQUEST_CHOOSE_PHOTO);
                } else if (items[item].equals(takeFromGallery)) {
                    // take photo from gallery
                    mPhotoChooser.dispatchTakePictureIntent(ChoosePhoto.PICK_IMAGE, REQUEST_CHOOSE_PHOTO);
                } else if (items[item].equals(cancel)) {
                    // close the dialog
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Request Haul
     */
    private void onRequestHaul() {
        if (mPickupInfo == null) {
            showToast(R.string.error_no_select_pickup_location);
            return;
        }

        if (mDropOffInfo == null) {
            showToast(R.string.error_no_select_dropoff_location);
            return;
        }

        if (mItemImagePath == null) {
            showToast(R.string.error_no_image);
            return;
        }

        if (mPrice == 0.0) {
            showToast(R.string.error_no_price);
            return;
        }

        // Request token for payment
        mWaitDialog.show();
        new RestAdapter.Builder()
                .setEndpoint(Settings.getEnvironmentUrl())     // Sandbox
                .setRequestInterceptor(new ApiClientRequestInterceptor())
                .build()
                .create(ApiClient.class)
                .getClientToken(
                    new Callback<ClientToken>() {
                        @Override
                        public void success(ClientToken clientToken, Response response) {
                            mWaitDialog.dismiss();

                            if (TextUtils.isEmpty(clientToken.getClientToken())) {
                                showToast("Unable to initialize payment. Please retry");
                            } else {
                                Intent intent = new Intent(RequestHaulActivity.this, PayHaulActivity.class);
                                intent.putExtra(PARAM_PICKUP_LOCATION, mPickupInfo);
                                intent.putExtra(PARAM_DROPOFF_LOCATION, mDropOffInfo);
                                intent.putExtra(PARAM_PRICE, mPrice);
                                intent.putExtra(PARAM_DISTANCE, mDistance);
                                intent.putExtra(PARAM_DURATION, mDuration);
                                intent.putExtra(PARAM_ITEM_IMAGE_PATH, mItemImagePath);
                                intent.putExtra(PARAM_PAYMENT_TOKEN, clientToken.getClientToken()); // Set Token
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            mWaitDialog.dismiss();
                            showToast("Unable to get a client token. Please check your network status or retry again.");
                        }
                    });


        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Activity Result
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICKUP_LOCATION) {
            if (data != null)
            {
                mPickupInfo = data.getParcelableExtra(PARAM_LOCATION);
                if (mPickupInfo != null) {
                    tvPickUpLocation.setText(mPickupInfo.getAddress());
                    estimatePrice();
                }
            }
        }

        else if (requestCode == REQUEST_DROPOFF_LOCATION) {
            if (data != null)
            {
                mDropOffInfo = data.getParcelableExtra(PARAM_LOCATION);
                if (mDropOffInfo != null) {
                    tvDropOffLocation.setText(mDropOffInfo.getAddress());
                    estimatePrice();
                }
            }
        }

        else if (requestCode == REQUEST_CHOOSE_PHOTO) {
            if (mPhotoChooser.getActionCode() == ChoosePhoto.PICK_IMAGE) {
                if (data != null) {
                    Uri targetUri = data.getData();
                    if (targetUri != null) {
                        mItemImagePath = mPhotoChooser.handleMediaPhoto(getApplicationContext(), targetUri, ivPhoto);
                    }
                }
            } else {
                if (resultCode != 0)
                    mItemImagePath = mPhotoChooser.handleCameraPhoto(ivPhoto);
            }
        }
    }

    /**
     * Estimate Price Based on Two Position
     */
    private void estimatePrice() {
        if (mPickupInfo == null || mDropOffInfo == null)
            return;

        mPrice = 0.0;   // Clear Price
        mDistance = "";
        mDuration = "";

        // Make LatLng
        double puLat, puLong;
        puLat = mPickupInfo.getLatitude();
        puLong = mPickupInfo.getLongitude();

        double doLat, doLong;
        doLat = mDropOffInfo.getLatitude();
        doLong = mDropOffInfo.getLongitude();

        LatLng pickup, dropoff;
        pickup = new LatLng(puLat, puLong);
        dropoff = new LatLng(doLat, doLong);

        // Calculate distance and duration time by track
        mWaitDialog.show();
        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                mWaitDialog.dismiss();

                try {
                    mDistance = gd.getTotalDistanceText(doc);
                    mDuration = gd.getTotalDurationText(doc);

                    int totalDistance = gd.getTotalDistanceValue(doc);
                    int totalDuration = gd.getTotalDurationValue(doc);

                    double price = HaulrUtil.getEstimatedPrice(totalDistance, totalDuration);

                    tvPrice.setText(String.format("$%.2f", price));

                    mPrice = price;
                }
                catch (Exception e)
                {
                    showToast(R.string.failed_calculation_estimation);
                    return;
                }
            }
        });

        gd.request(pickup, dropoff, GoogleDirection.MODE_DRIVING);
    }
}