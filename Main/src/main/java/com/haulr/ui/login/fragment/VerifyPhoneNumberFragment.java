package com.haulr.ui.login.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haulr.R;
import com.haulr.parse.ParseSession;
import com.haulr.parse.model.User;
import com.haulr.utils.HaulrUtil;

/**
* @description  Verify Code Fragment
 * @author       Adrian
 */
public class VerifyPhoneNumberFragment extends BaseFragment {
    TextView etVerifyCode;
    TextView tvResend;
    TextView tvDriverLoginHint;

    User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = ParseSession.getCurrentUser(getActivity());
    }

    @Override
    public void requestGoNext(OnNextStepRequestListener listener) {
        super.requestGoNext(listener);

        final String verifyCode = etVerifyCode.getText().toString();
        if (verifyCode == null || verifyCode.isEmpty()) {
            showToast(R.string.label_info_code);
            if (listener != null) listener.OnError();
            return;
        }

        // Check Code

        if (mUser != null) {
            String code = mUser.getVerifyCode();
            if (code.equals(verifyCode)) {

                // Save verified.
                mUser.setVerified(true);
                mUser.saveInBackground();

                if (isLoginAsDriver && !mUser.allowDriver()) {
                    showToast(R.string.error_login_as_driver);
                    return;
                }

                // Go to main page
                if (mOnNextStepRequestListener != null)
                    mOnNextStepRequestListener.OnGoMain();
            } else {
                showToast(R.string.error_invalid_code);
                if (mOnNextStepRequestListener != null)
                    mOnNextStepRequestListener.OnError();
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_verify_phone_number, null);
        etVerifyCode = (TextView)v.findViewById(R.id.etVerifyCode);

        tvResend = (TextView)v.findViewById(R.id.tvResend);
        tvResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = ParseSession.getCurrentUser(getActivity());
                if (user != null) {
                    sendVerifyCodeMessage(user, user.getPhoneNumber(), makeVerifyCode());
                }
            }
        });

        tvDriverLoginHint = (TextView)v.findViewById(R.id.tvDriverLoginHint);
        tvDriverLoginHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HaulrUtil.sendEmail(getActivity(), mUser.getPhoneNumber());
            }
        });
        if (isLoginAsDriver)
            tvDriverLoginHint.setVisibility(View.VISIBLE);

        return v;
    }
}
