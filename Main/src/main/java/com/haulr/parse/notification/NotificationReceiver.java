package com.haulr.parse.notification;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.haulr.R;
import com.haulr.parse.ParseSession;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.parse.model.User;
import com.haulr.ui.common.PendingHaulActivity;
import com.haulr.ui.driver.dTicketActivity;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @description     Push Notification Receiver
 * @author          Adrian
 */
public class NotificationReceiver extends ParsePushBroadcastReceiver {

    private Intent parseIntent;

    public NotificationReceiver() {
        super();
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        //super.onPushReceive(context, intent);

        if (intent == null)
            return;

        try {
            // If user switched to offline already, there is no need to show notification
            User user = ParseSession.getCurrentUser(context);
            if (user == null || !user.asDriver() || !user.isOnline())
                return;

            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            parseIntent = intent;
            parsePushJson(context, json);
        } catch (JSONException e) {
            Log.e(ParsePushBroadcastReceiver.class.getSimpleName(), "Push message json exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    /**
     * Parses the push notification json
     *
     * @param context
     * @param json
     */
    private void parsePushJson(Context context, JSONObject json) {
        try {
            String serviceId = json.getString("serviceId");
            String status = json.getString("status");

            if (status.equals(ServiceStatus.PENDING.toString())) {
                String message = context.getString(R.string.service_request_received);

                Intent intent = new Intent(context, dTicketActivity.class);
                intent.putExtras(parseIntent.getExtras());
                intent.putExtra(dTicketActivity.PARAM_SERVICE_ID, serviceId);

                //intent.putExtra("where", json.getString("where"));
                NotificationManager.getInstance().showNotificationMessage(context, context.getString(R.string.app_name), message, intent);
            } else {

            }

        } catch (JSONException e) {
            Log.e(this.getClass().getSimpleName(), "Push message json exception: " + e.getMessage());
        }
    }

}