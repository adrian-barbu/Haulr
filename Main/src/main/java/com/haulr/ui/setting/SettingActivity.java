package com.haulr.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.View;

import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.ui.BaseActivity;
import com.haulr.ui.MainActivity;
import com.parse.ParseException;
import com.parse.SaveCallback;

/**
 * @description     Setting Activity
 * @author           Adrian
 */
public class SettingActivity extends BaseActivity {

    // UI Controls
    SwitchCompat switchMoverMode;

    WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mWaitDialog = new WaitDialog(this);

        if (mCurrentUser.asDriver()) {
            // Hide Payment & Mover Mode
            ((View) findViewById(R.id.layoutPayment)).setVisibility(View.GONE);
            ((View) findViewById(R.id.layoutDriverMode)).setVisibility(View.GONE);
            ((View) findViewById(R.id.separator1)).setVisibility(View.GONE);
        } else {
            switchMoverMode = (SwitchCompat) findViewById(R.id.switchMoverMode);
            switchMoverMode.setChecked(mCurrentUser.asDriver());
        }
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
        switch(view.getId()) {
            case R.id.layoutProfile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;

            case R.id.layoutPayment:
                startActivity(new Intent(this, PaymentActivity.class));
                break;

            case R.id.layoutDriverMode:
                startActivity(new Intent(this, DriverModeActivity.class));
                break;

            case R.id.layoutAbout:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.tvBack:
                onBack();
                break;
        }
    }

    private void onBack() {
        /*
        // Check if user changed its mode
        boolean isSelectDriverMode = switchMoverMode.isChecked();
        if (isSelectDriverMode != mCurrentUser.asDriver()) {
            // Changed status
            mWaitDialog.show();

            mCurrentUser.setAsDriver(isSelectDriverMode);
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    mWaitDialog.dismiss();

                    if (e == null) {
                        // Start new main activity
                        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        finish();
                    } else {
                        showToast(R.string.error_change_mode);
                    }
                }
            });
        } else {
            finish();
        }
        */

        finish();
    }
}