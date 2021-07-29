package com.haulr.parse;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.haulr.R;
import com.haulr.UILApplication;
import com.haulr.parse.model.User;
import com.haulr.parse.notification.NotificationManager;
import com.haulr.utils.CommonUtil;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * @description     Parse Session Management
 *
 * @author          Adrian
 */
public class ParseSession {

    /**
     * Get Logined User
     *
     * @return
     */
    public static User getCurrentUser(Context context) {
        if (checkNetworkConnectivity(context))
            return (User) ParseUser.getCurrentUser();
        else
            return null;
    }

    /**
     * Log Out From Current User
     */
    public static void logOut(Context context) {
        User user = getCurrentUser(context);
        if (user != null) {
            if (user.asDriver())
                NotificationManager.getInstance().unSubscribe();

            user.logOutInBackground();
        }
    }

    /**
     * Check Network Connectivity
     */
    public static boolean checkNetworkConnectivity(Context context) {
        if (!CommonUtil.isNetworkAvailable(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog);
            builder.setTitle(R.string.app_name)
                    .setMessage(R.string.error_network_connectivity)
                    .setCancelable(true)
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            return;
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        } else {
            return true;
        }
    }
}
