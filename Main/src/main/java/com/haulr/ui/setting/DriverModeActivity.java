package com.haulr.ui.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.haulr.R;
import com.haulr.ui.BaseActivity;
import com.haulr.utils.HaulrUtil;

/**
 * @description     Mover Mode Activity
 * @author          Adrian
 */
public class DriverModeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_driver_mode);
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

    /**
     *      On Item Click Listener
     *
     * @param view
     */
    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.layoutDriver:
                HaulrUtil.sendEmail(this, mCurrentUser.getPhoneNumber());
                break;

            case R.id.ivBack:
                finish();
                break;
        }
    }
}