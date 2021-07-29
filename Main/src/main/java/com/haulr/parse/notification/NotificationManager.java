package com.haulr.parse.notification;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.haulr.R;
import com.haulr.parse.ParseSession;
import com.haulr.parse.model.ServiceStatus;
import com.haulr.parse.model.User;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.util.List;

/**
 * @description     Push Notification Manager
 * @author          Adrian
 */
public class NotificationManager {
    public static NotificationManager instance;

    public static NotificationManager getInstance() {
        if (instance == null)
            instance = new NotificationManager();

        return instance;
    }

    public final static String PARSE_CHANNEL = "Haulr";
    public final int NOTIFICATION_ID = 100;

    /**
     *  SubScribe For Send & Receive Push Notification
     *
     *  @params
     */
    public void subscribe(final Context context) {
        // initializing parse library
        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParsePush.subscribeInBackground(PARSE_CHANNEL, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(context, R.string.subscribe_channel_failed, Toast.LENGTH_SHORT).show();
                } else {
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Toast.makeText(context, R.string.subscribe_channel_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     *  UnSubScribe Push Notification
     */
    public void unSubscribe() {
        try {
            ParsePush.unsubscribeInBackground(PARSE_CHANNEL);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     *  Send Push Notification
     *
     * @params
     *      serviceId : service id on Parse
     *      status : service status
     */
    public void sendNotification(String serviceId, ServiceStatus status)
    {
        // Create our Installation query
        ParseQuery pushQuery = ParseInstallation.getQuery();
        //pushQuery.whereEqualTo("userId", userId);

        // Send push notification to query
        ParsePush push = new ParsePush();
        push.setQuery(pushQuery); // Set our Installation query
        push.setData(makeJsonData(serviceId, status));
        push.sendInBackground();
    }

    /**
     *  Make JSON Data For Push Notification
     *
     * @params
     *      serviceId : service id on Parse
     */
    public static JSONObject makeJsonData(String serviceId, ServiceStatus status) {
        try {
            JSONObject object = new JSONObject();
            object.put("serviceId", serviceId);
            object.put("status", status.toString());
            return object;
        } catch (Exception ex)
        {
            return null;
        }
    }

    /**
     *  Show Notification Message
     *
     * @params
     */
    public void showNotificationMessage(Context context, String title, String message, Intent intent) {

        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK /* | Intent.FLAG_ACTIVITY_CLEAR_TASK */);

        if (isAppIsInBackground(context)) {
            // notification icon
            int icon = R.mipmap.ic_launcher;

            int mNotificationId = NOTIFICATION_ID;

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            Notification notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentIntent(resultPendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
                    .setContentText(message)
                    .build();

            android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(mNotificationId, notification);
        }
        else {
            context.startActivity(intent);
        }
    }

    /**
     * Method checks if the app is in background or not
     *
     * @param context
     * @return
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}
