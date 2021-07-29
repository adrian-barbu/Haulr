package com.haulr.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.haulr.R;

/**
 * @description     Private Utils
 * @author          Adrian
 */
public class HaulrUtil {

    /**
     * Calculate Estimated Price
     *
     * @param distanceInMeter
     * @param durationInSec
     * @return
     */
    public static double getEstimatedPrice(int distanceInMeter, int durationInSec) {
        double BASE_FEE = 30.00, COST_PER_KM = 0.75, COST_PER_MIN = 0.5;
        double distanceFee = (distanceInMeter / 1000.0) * COST_PER_KM;
        double durationFee = (durationInSec / 60.0) * COST_PER_MIN;
        return BASE_FEE + distanceFee + durationFee;
    }

    /**
     * Check Email Pattern
     *
     * @param target
     * @return
     */
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * Send Email
     */
    public static void sendEmail(Context context, String phoneNumber) {
        String message = String.format("%s\r\nMy phone number is %s",
                context.getString(R.string.setting_driver_request), phoneNumber);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "hello@haulr.co", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
