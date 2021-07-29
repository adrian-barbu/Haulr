package com.haulr.twilio;

import android.os.AsyncTask;
import android.util.Base64;

//import com.twilio.sdk.TwilioRestClient;
//import com.twilio.sdk.TwilioRestException;
//import com.twilio.sdk.resource.factory.MessageFactory;
//import com.twilio.sdk.resource.instance.Account;
//import com.twilio.sdk.resource.instance.Message;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *  @description    Send SMS Module through twilio
 *
 *  @author          Adrian
 */
public class SendSMS {
    public static final String ACCOUNT_SID = "AC8491c1798e97b5fe4f3ea8c9abdd09b5";
    public static final String AUTH_TOKEN = "f8d6aedbc80ac01edbea650d7dddac72";

//    public static final String MY_PHONE_NUMBER = "+16043085991";
    public static final String MY_PHONE_NUMBER = "+17782000705";

    public interface OnSendMessageListener {
        void onMessageSent(String message);
        void onError(String message);
    };

//    public static String sendMessage(String toPhoneNumber, String message, OnSendMessageListener listener) {
//
//        try {
//            TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
//
//            Account account = client.getAccount();
//
//            MessageFactory messageFactory = account.getMessageFactory();
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("To", MY_PHONE_NUMBER));
//            params.add(new BasicNameValuePair("From", MY_PHONE_NUMBER));
//            params.add(new BasicNameValuePair("Body", message));
//            Message sms = messageFactory.create(params);
//            String sid = sms.getSid();
//            return sid;
//        } catch (Exception e) {
//            return "error";
//        }
//    }

    public static void sendMessage(String to, String message, OnSendMessageListener listener) {
        new SendMessageTask(listener).execute(new String[] {to, message});
    }

    /**
     * Send Message Task
     */
    static class SendMessageTask extends AsyncTask<String, Void, String> {

        private Exception exception;
        private OnSendMessageListener mOnSendMessageListener;

        public SendMessageTask(OnSendMessageListener listener) {
            mOnSendMessageListener = listener;
        };

        protected String doInBackground(String... params) {
            String to = params[0];
            if (!to.startsWith("+"))        // This is twilio's recommendation
                to = "+" + to;

            String message = params[1];

            HttpClient httpclient = new DefaultHttpClient();

            String postUrl = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/SMS/Messages.json", ACCOUNT_SID);
            HttpPost httppost = new HttpPost(postUrl);
            String base64EncodedCredentials = "Basic "
                    + Base64.encodeToString(
                    (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(),
                    Base64.NO_WRAP);

            httppost.setHeader("Authorization", base64EncodedCredentials);

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("From", MY_PHONE_NUMBER));
                nameValuePairs.add(new BasicNameValuePair("To", to));
                nameValuePairs.add(new BasicNameValuePair("Body", message));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();

                String result =  EntityUtils.toString(entity);
                return result;

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                // Parse Result
                JSONObject object = new JSONObject(result);

                try {
                    String sid = object.getString("sid");
                    if (sid != null && !sid.isEmpty()) {
                        if (mOnSendMessageListener != null)
                            mOnSendMessageListener.onMessageSent(sid);

                        return;
                    }
                } catch (Exception e) {
                }

                String message = object.getString("message");
                if (mOnSendMessageListener != null)
                    mOnSendMessageListener.onError(message);

            } catch (Exception e) {
                if (mOnSendMessageListener != null)
                    mOnSendMessageListener.onError("Error");
            }
        }
    }
}
