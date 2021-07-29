package com.haulr.ui.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.parse.model.UserInfo;
import com.haulr.parse.notification.NotificationManager;
import com.haulr.ui.login.LoginActivity;
import com.haulr.parse.ParseEngine;
import com.haulr.parse.ParseSession;
import com.haulr.ui.BaseActivity;
import com.haulr.utils.HaulrUtil;
import com.parse.ParseException;

/**
 * @description     Profile Activity
 * @author           Adrian
 */
public class ProfileActivity extends BaseActivity {

    // UI Controls
    EditText etFirstName, etLastName;
    EditText etEmail, etPhoneNumber;

    WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);

        mWaitDialog = new WaitDialog(this);

        initUI();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    private void initUI()
    {
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);

        // Fill datas
        etPhoneNumber.setText(mCurrentUser.getUsername());

        UserInfo userInfo = mCurrentUser.getUserInfo();
        if (userInfo != null) {
            try {
                userInfo.fetchIfNeeded();

                etFirstName.setText(userInfo.getFirstName());
                etLastName.setText(userInfo.getLastName());
                etEmail.setText(userInfo.getEmail());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *      On Item Click Listener
     *
     * @param view
     */
    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.layoutLogout:
                onLogout();
                break;

            case R.id.tvUpdate:
                onUpdate();
                break;

            case R.id.ivBack:
                finish();
                break;
        }
    }

    /**
     * Update User Info
     */
    private void onUpdate() {
        String phoneNumber = etPhoneNumber.getText().toString();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            showToast(R.string.error_input_phone_number);
            return;
        }

        String email = etEmail.getText().toString();
        if (email != null && !email.isEmpty() && !HaulrUtil.isValidEmail(email)) {
            showToast(R.string.error_invalid_email);
            return;
        }

        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();

        // Start to update
        mWaitDialog.show();
        ParseEngine.setUserInfo(this, firstName, lastName, email, phoneNumber, new ParseEngine.OnParseOperationListener() {
            @Override
            public void onSuccess(Object data) {
                mWaitDialog.dismiss();
                showToast(R.string.success_update_user_info);
            }

            @Override
            public void onFailed(String error) {
                mWaitDialog.dismiss();
                showToast(R.string.failed_update_user_info);
            }
        });
    }

    /**
     * Log Out
     */
    private void onLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.warning_logout)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // LogOut
                        ParseSession.logOut(getApplicationContext());

                        // UnSubscribe
                        NotificationManager.getInstance().unSubscribe();

                        // Goto Login Activity
                        Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                        // set the new task and clear flags
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);

                        finish();
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
}