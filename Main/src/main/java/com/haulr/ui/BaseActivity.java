package com.haulr.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.haulr.R;
import com.haulr.parse.ParseSession;
import com.haulr.parse.model.User;

/**
 * @description     Base Activity
 *                    This activity will be contain map viewer
 *
 * @author           Adrian
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Intent Params
    public final static String PARAM_LOCATION = "Location";
    public final static String PARAM_PICKUP_LOCATION = "PickupLocation";
    public final static String PARAM_DROPOFF_LOCATION = "DropOffLocation";
    public final static String PARAM_PRICE = "Price";
    public final static String PARAM_DISTANCE = "Distance";
    public final static String PARAM_DURATION = "Duration";
    public final static String PARAM_ITEM_IMAGE_PATH = "ItemImagePath";

    public final static String PARAM_DRIVER_STATUS = "DriverStatus";

    public final static String PARAM_PAYMENT_TOKEN = "PaymentToken";

    // Intent Params For Service
    public final static String PARAM_SERVICE_ID = "ServiceId";
    public final static String PARAM_SERVICE_INFO = "ServiceInfo";

    protected User mCurrentUser;      // Current user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentUser = ParseSession.getCurrentUser(this);
    }

    protected void onDestory() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return true;
    }

    /**
     * Common Function
     */
    protected void showToast(int res_id) {
        Toast.makeText(this, res_id, Toast.LENGTH_LONG).show();
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
