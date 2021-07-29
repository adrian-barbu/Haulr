package com.haulr.ui.login.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.parse.model.User;
import com.haulr.twilio.SendSMS;

import java.util.Random;

/**
 * @description Base Fragment For Login Setup
 * @author       Adrian
 */
public class BaseFragment extends Fragment {
    public final static String LOGIN_FROM_FACEBOOK = "loginFromFacebook";   // Params
    public final static String LOGIN_AS_DRIVER = "loginAsDriver";

    WaitDialog mWaitDialog;
    boolean isLoginFromFacebook;
    boolean isLoginAsDriver;
    OnNextStepRequestListener mOnNextStepRequestListener;       // Listener

    /**
     * Asynchorized Next Going Interface
     */
    public interface OnNextStepRequestListener {
        void OnAllow();     // Allow to go next
        void OnError();     // Not Allow to go next
        void OnGoMain();    // Go to main
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLoginFromFacebook = getArguments().getBoolean(LOGIN_FROM_FACEBOOK, false);
        isLoginAsDriver = getArguments().getBoolean(LOGIN_AS_DRIVER, false);

        mWaitDialog = new WaitDialog(getActivity());
    }

    /**
     * Request to go next
     * If all conditions will be allowed, they will send message to allow to go next via listener
     *
     * @param listener
     */
    public void requestGoNext(OnNextStepRequestListener listener) {
        mOnNextStepRequestListener = listener;
    }

    public void showToast(int res_id) {
        Toast.makeText(getActivity(), res_id, Toast.LENGTH_SHORT).show();
    }

    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    /*
     * Show waiting bar
     */
    public void loadingStart()
    {
        if (mWaitDialog != null && mWaitDialog.isShowing())
            mWaitDialog.dismiss();

        mWaitDialog.show();
    }

    /*
	 * Hide waiting bar
	 */
    public void loadingFinish()
    {
        if (mWaitDialog != null && mWaitDialog.isShowing())
            mWaitDialog.dismiss();
    }

    /**
     * Send Verify Code Message
     */
    public void sendVerifyCodeMessage(final User user, String phoneNumber, final String verifyCode) {
        final String message = String.format(getString(R.string.message_send_code), verifyCode);

        loadingStart();

        SendSMS.sendMessage(phoneNumber, message, new SendSMS.OnSendMessageListener() {
            @Override
            public void onMessageSent(String message) {

                user.setVerifyCode(verifyCode);
                user.setVerifyCodeSent(true);
                user.saveInBackground();

                loadingFinish();

                if (mOnNextStepRequestListener != null) {
                    mOnNextStepRequestListener.OnAllow();
                }
            }

            @Override
            public void onError(String message) {
                loadingFinish();

                // Clear this user because this user will not be used
                user.logOutInBackground();
                user.deleteInBackground();

                // Send error message
                showToast(message);

                if (mOnNextStepRequestListener != null) {
                    mOnNextStepRequestListener.OnError();
                }
            }
        });
    }

    /**
     *  Make Random Code
     */
    protected String makeVerifyCode() {
        int min = 10000;
        int max = 99999;
        Random r = new Random();
        return String.valueOf(r.nextInt(max - min + 1) + min);
    }

}
