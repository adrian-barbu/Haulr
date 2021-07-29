package com.haulr.ui.customer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.braintreepayments.api.Braintree;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.PaymentMethod;

import com.haulr.control.dialog.WaitDialog;
import com.haulr.parse.model.PlaceInfo;
import com.haulr.parse.ParseEngine;
import com.haulr.parse.ParseServiceEngine;
import com.haulr.parse.model.CreditCard;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.parse.model.UserInfo;
import com.haulr.parse.notification.NotificationManager;
import com.haulr.payment.Settings;
import com.haulr.payment.internal.ApiClient;
import com.haulr.payment.internal.ApiClientRequestInterceptor;
import com.haulr.payment.models.Transaction;
import com.haulr.ui.BaseActivity;
import com.haulr.ui.MainActivity;
import com.haulr.ui.setting.PaymentActivity;
import com.haulr.utils.CommonUtil;
import com.haulr.utils.ImageUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.haulr.R;

import org.json.JSONObject;

/**
 * @description     PayHaul Activity
 * @author           Adrian
 */
public class PayHaulActivity extends BaseActivity implements
        Braintree.PaymentMethodCreatedListener, Braintree.ErrorListener, Braintree.PaymentMethodNonceListener {

    /**
     * Request code passed to the CardSelectionActivity
     */
    private static final int REQUEST_PICK_CARD = 1;

    // Variables
    private PlaceInfo mPickupInfo, mDropOffInfo;
    private String mDistance, mDuration;
    private double mPrice;
    private String mImagePath;

    protected CreditCard mCreditCard;

    // Payment
    private String mClientToken;
    private Braintree mBraintree;
    private String mNonce;
    private String mCustomerId;
    private String mTransactionID;

    // UI Members
    TextView tvPrice;
    TextView tvCreditCardInfo;
    TextView tvPayment;         // Payment Button
    View layoutProgress;

    View layoutPayStart, layoutPayDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_haul);

        getParams();
        initUI();

        initPayment();
    }

    private void getParams() {
        mPickupInfo = getIntent().getParcelableExtra(PARAM_PICKUP_LOCATION);
        mDropOffInfo = getIntent().getParcelableExtra(PARAM_DROPOFF_LOCATION);
        mDistance = getIntent().getStringExtra(PARAM_DISTANCE);
        mDuration = getIntent().getStringExtra(PARAM_DURATION);
        mImagePath = getIntent().getStringExtra(PARAM_ITEM_IMAGE_PATH);

        mPrice = getIntent().getDoubleExtra(PARAM_PRICE, 0.0);
        mPrice = CommonUtil.roundDouble(mPrice);

        mClientToken = getIntent().getStringExtra(PARAM_PAYMENT_TOKEN);
    }

    private void initUI() {
        layoutProgress = (View) findViewById(R.id.layoutProgress);

        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvPrice.setText(String.format("$%.2f", mPrice));

        tvCreditCardInfo = (TextView) findViewById(R.id.tvCreditCardInfo);
        tvPayment = (TextView) findViewById(R.id.tvPayment);

        layoutPayStart = (View) findViewById(R.id.layoutPayStart);
        layoutPayDone = (View) findViewById(R.id.layoutPayDone);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    public void onItemClick(View view) {
        Intent intent;

        switch(view.getId()) {
            case R.id.layoutPayStart:   // Start Pay
                doStartPayment();
                break;

            case R.id.layoutPayDone:    // Start to register new request
                doRegisterService();
                break;

            /*
            case R.id.tvPayment:
                doStartPayment();
                break;
            */

            case R.id.layoutCardSelection:
                intent = new Intent(this, PaymentActivity.class);
                intent.putExtra(PaymentActivity.PARAM_SELECT_CARD, true);
                startActivityForResult(intent, REQUEST_PICK_CARD);
                break;

            case R.id.tvPaymentCancel: {
                finish();

                intent = new Intent(this, RequestHaulActivity.class);
                startActivity(intent);

                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            }
        }
    }

    /**
     * Start to payment using Braintree
     */
    private void doStartPayment() {
        // Check Credit Info
        if (mCreditCard == null || mCreditCard.getCardNumber().isEmpty() || mCreditCard.getExpireDate().isEmpty()) {
            showToast(R.string.warning_select_credit_card);
            return;
        }

        showLoadingView();

        // Get User Info
        String fName = "", lName = "", email = "";

        try {
            UserInfo userInfo = mCurrentUser.getUserInfo();
            userInfo.fetchIfNeeded();

            fName = userInfo.getFirstName();
            lName = userInfo.getLastName();
            email = userInfo.getEmail();
        } catch (Exception e) {
        }

        // Create Nonce From Credit Card
        CardBuilder cardBuilder = new CardBuilder()
                .cardNumber(mCreditCard.getCardNumber())
                .expirationMonth(mCreditCard.getExpireMonth())
                .expirationYear(mCreditCard.getExpireYear())
                .validate(true) /* validate or not */;

        if (!mCreditCard.getCVC().isEmpty())
            cardBuilder.cvv(mCreditCard.getCVC());

        if (fName != null && !fName.isEmpty())
            cardBuilder.firstName(fName);

        if (lName != null && !lName.isEmpty())
            cardBuilder.lastName(lName);

        if (email != null && !email.isEmpty())
            cardBuilder.streetAddress(email);

        mBraintree.tokenize(cardBuilder);
    }

    /**
     * Register New Service
     */
    private void doRegisterService() {
        showLoadingView();

        // First, load image
        if (mImagePath != null && !mImagePath.isEmpty()) {
            ImageUtil.loadImage("file://" + mImagePath, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    safelyCloseLoadingView();
                    showToast(R.string.failed_create_service);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                    createNewService(loadedBitmap);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                }
            });
        } else {
            createNewService(null);
        }
    }

    /**
     * Create New Service
     *
     * @param loadedBitmap
     */
    private void createNewService(final Bitmap loadedBitmap) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] datas = null;

        try {
            loadedBitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
            datas = stream.toByteArray();
        } catch (Exception e)
        {
        }

        ParseServiceEngine.createNewService(this, mPickupInfo, mDropOffInfo, mDistance, mDuration, mPrice, mCreditCard.getCardNumber(), mTransactionID, datas,
                new ParseEngine.OnParseOperationListener() {
                    @Override
                    public void onSuccess(Object data) {
                        try {
                            stream.close();
                            loadedBitmap.recycle();
                            System.gc();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        safelyCloseLoadingView();
                        //showToast(R.string.success_create_service);

                        String serviceId = (String) data;

                        // Send notification
                        if (Settings.USE_NOTIFICATION)
                            NotificationManager.getInstance().sendNotification(serviceId, ServiceStatus.PENDING);

                        // Go Monitoring Activity
                        Intent intent = new Intent(PayHaulActivity.this, MonitoringHaulActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(PARAM_SERVICE_ID, serviceId);
                        startActivity(intent);

                        finish();
                    }

                    @Override
                    public void onFailed(String error) {
                        // Release Bitmap
                        try {
                            stream.close();
                            loadedBitmap.recycle();
                            System.gc();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        safelyCloseLoadingView();
                        showToast(R.string.failed_create_service);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_CARD:     // Get Credit Card
                if (resultCode == RESULT_OK) {
                    mCreditCard = data.getParcelableExtra(PaymentActivity.PARAM_SELECTED_CARD);
                    String cardNumber = mCreditCard.getCardNumber();
                    cardNumber = cardNumber.replace("-", "");
                    tvCreditCardInfo.setText(String.format("(%s)", CommonUtil.hideCardNumber(cardNumber)));
                }
                break;
        }
    }


    /**
     * Payment Functions
     */
    private void initPayment() {
        Braintree.setup(this, mClientToken, new Braintree.BraintreeSetupFinishedListener() {
            @Override
            public void onBraintreeSetupFinished(boolean setupSuccessful,
                                                 Braintree braintree,
                                                 String errorMessage, Exception exception) {
                safelyCloseLoadingView();

                if (setupSuccessful) {
                    mBraintree = braintree;
                    mBraintree.addListener(PayHaulActivity.this);
                    enableButtons(true);
                } else {
                    showToast(errorMessage);
                }
            }
        });

        enableButtons(true);
    }

    private void enableButtons(boolean enable) {
        tvPayment.setBackgroundResource(R.drawable.common_button_selector);
        tvPayment.setEnabled(enable);
    }

    @Override
    public void onUnrecoverableError(Throwable throwable) {
        safelyCloseLoadingView();
        showToast("An unrecoverable error was encountered (" +
                throwable.getClass().getSimpleName() + "): " + throwable.getMessage());
    }

    @Override
    public void onRecoverableError(ErrorWithResponse error) {
        safelyCloseLoadingView();
        showToast("A recoverable error occurred: " + error.getMessage());
    }

    @Override
    public void onPaymentMethodCreated(PaymentMethod paymentMethod) {
        safelyCloseLoadingView();
    }

    @Override
    public void onPaymentMethodNonce(String paymentMethodNonce) {
        mNonce = paymentMethodNonce;

        showLoadingView();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendNonceToServer(mNonce);
            }
        }, 2000);
    }

    private void sendNonceToServer(final String nonce) {
        if (mBraintree != null) {
            mBraintree.unlockListeners();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("nonce", nonce);
        params.put("amount", mPrice);
        params.put("instant_submit", "false");     // No Instant submit
        client.post(Settings.getPaymentUrl(), params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        safelyCloseLoadingView();

                        // Parse Result
                        try {
                            String response = new String(responseBody);

                            JSONObject object = new JSONObject(response);
                            boolean success = object.getBoolean("success");
                            String message = object.getString("msg");
                            String transactionID = object.getString("transaction_id");

                            if (success && !transactionID.isEmpty()) {
                                mTransactionID = transactionID;

                                // Enable Done Layout
                                layoutPayStart.setVisibility(View.GONE);
                                layoutPayDone.setVisibility(View.VISIBLE);
                            } else {
                                // Fail
                                showToast(R.string.error_pay_with_credit_card);
                            }
                        }
                        catch (Exception e)
                        {
                            showToast(R.string.error_pay_with_credit_card);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        safelyCloseLoadingView();

                        showToast(R.string.error_create_transaction);
                    }
                }
        );
    }

    private void showLoadingView() {
        layoutProgress.setVisibility(View.VISIBLE);
    }

    private void safelyCloseLoadingView() {
        layoutProgress.setVisibility(View.GONE);
    }
}