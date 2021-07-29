package com.haulr.ui.login.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.haulr.R;
import com.haulr.parse.ParseSession;
import com.haulr.parse.model.User;
import com.haulr.utils.CommonUtil;
import com.haulr.utils.HaulrUtil;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.Random;

/**
* @description  Phone Number Input Fragment
 * @author       Adrian
 */
public class PhoneNumberFragment extends BaseFragment {
    EditText etPhoneNumber;
    TextView tvHint;

    private final static String PASSWORD = "Haulr";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_input_phone_number, null);

        etPhoneNumber = (EditText)v.findViewById(R.id.etPhoneNumber);

        User user = ParseSession.getCurrentUser(getActivity());
        if (user != null) {
            String phoneNumber = user.getPhoneNumber();
            if (phoneNumber != null)
                etPhoneNumber.setText(phoneNumber);
        }

        tvHint = (TextView)v.findViewById(R.id.tvHint);
        if (isLoginAsDriver) {
            tvHint.setText(R.string.label_info_phone_number_for_driver);
            tvHint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String phoneNumber = etPhoneNumber.getText().toString();
                    if (phoneNumber.isEmpty())
                    {
                        showToast(R.string.error_input_phone_number);
                        return;
                    }

                    HaulrUtil.sendEmail(getActivity(), etPhoneNumber.getText().toString());
                }
            });
        } else {
            tvHint.setText(R.string.label_info_phone_number_for_customer);
        }

        return v;
    }

    @Override
    public void requestGoNext(OnNextStepRequestListener listener) {
        super.requestGoNext(listener);

        boolean valid = true;

        // This is temp processing for phone number, need to insert the verification module for phone number
        final String number = etPhoneNumber.getText().toString();
        if (number == null || number.isEmpty()) {
            showToast(R.string.error_input_phone_number);
            if (listener != null) listener.OnError();
            return;
        }

        //final String phoneNumber = number.replace("+", "").replace("(", "").replace(" ", "").replace("-", "");
        final String phoneNumber = CommonUtil.getE164PhoneNumber(getActivity(), number);

        if (!ParseSession.checkNetworkConnectivity(getActivity()))
            return;

        // Login First
        loadingStart();

        if (isLoginFromFacebook)
        {
            final String verifyCode = makeVerifyCode();

            final User user = ParseSession.getCurrentUser(getActivity());
            user.setUsername(phoneNumber);
            user.setPassword(PASSWORD);
            user.setPhoneNumber(phoneNumber);
            user.setVerified(false);
            user.setVerifyCode(verifyCode);

            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    loadingFinish();

                   if (e == null) {
                       sendVerifyCodeMessage(user, phoneNumber, verifyCode);
                   } else {
                       onError();
                   }
                }
            });
        }
        else        // Common Users
        {
            User.logInInBackground(phoneNumber, PASSWORD, new LogInCallback() {

                @Override
                public void done(ParseUser pUser, ParseException e) {
                    loadingFinish();

                    User user = (User) pUser;

                    if (user != null)    // User is already
                    {
                        if (isLoginAsDriver && !user.allowDriver()) {
                            showToast(R.string.error_login_as_driver);
                            return;
                        }

                        String verifyCode = makeVerifyCode();
                        // Send Verification Code via Message
                        sendVerifyCodeMessage(user, phoneNumber, verifyCode);

                        /*
                        // Check Whether is verified or not
                        boolean verified = user.isVerified();
                        if (verified) {
                            if (isLoginAsDriver && !user.allowDriver()) {
                                showToast(R.string.error_login_as_driver);
                                return;
                            }

                            // Go to main page
                            if (mOnNextStepRequestListener != null)
                                mOnNextStepRequestListener.OnGoMain();
                        } else {
                            boolean verifyCodeSent = user.isVerifyCodeSent();
                            if (verifyCodeSent) {
                                if (mOnNextStepRequestListener != null)
                                    mOnNextStepRequestListener.OnAllow();
                            } else {
                                String verifyCode = user.getVerifyCode();
                                // Send Verification Code via Message
                                sendVerifyCodeMessage(user, phoneNumber, verifyCode);
                            }
                        }
                        */

                    } else    // User is not registered, register new
                    {
                        registerNewUser(phoneNumber);
                    }
                }
            });
        }
    }

    /*
    *   Register New User
     */
    private boolean registerNewUser(final String phoneNumber) {
        boolean success = false;

        if (!ParseSession.checkNetworkConnectivity(getActivity()))
        {
            onError();
            return false;
        }

        final String verifyCode = makeVerifyCode();

        // Register as new
        final User newUser = new User(phoneNumber, verifyCode);
        newUser.setPassword(PASSWORD);

        newUser.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                loadingFinish();
                if (e == null) {
                    sendVerifyCodeMessage(newUser, phoneNumber, verifyCode);
                } else {
                    onError();
                }
            }
        });

        return true;
    }

    private void onError() {
        showToast(R.string.error_login);

        if (mOnNextStepRequestListener != null)
            mOnNextStepRequestListener.OnError();
    }
}
