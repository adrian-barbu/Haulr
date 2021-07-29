package com.haulr.ui.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.haulr.R;
import com.haulr.ui.BaseActivity;

/**
 * @description     Driver Changed Status Activity
 * @author          Adrian
 */
public class dChangeStatusActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_change_status);
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
            case R.id.layoutOnline:
                setResult(true /* online */);
                break;

            case R.id.layoutOffline:
                setResult(false /* offline */);
                break;

            case R.id.ivBack:
                finish();
                break;
        }
    }

    /**
     * Return With Value
     */
    private void setResult(boolean online) {
        Intent intent = new Intent();
        intent.putExtra(PARAM_DRIVER_STATUS, online);
        setResult(RESULT_OK, intent);
        finish();
    }
}