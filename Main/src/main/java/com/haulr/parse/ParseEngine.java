package com.haulr.parse;

import android.content.Context;

import com.haulr.parse.model.CreditCard;
import com.haulr.parse.model.User;
import com.haulr.parse.model.UserInfo;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.SaveCallback;

import java.util.ArrayList;

/**
 * @description     Parse Engine
 *                  Include most of functions relation to parse
 *
 * @author          Adrian
 */
public class ParseEngine {

    /**
     * Callback interface for every operation
     */
    public interface OnParseOperationListener {
        void onSuccess(Object data);
        void onFailed(String error);
    }

    /**
     * Set User Info
     *
     * @param firstName
     * @param lastName
     * @param email
     * @param phoneNumber
     *
     * @return
     */
    public static boolean setUserInfo(Context context, String firstName, String lastName, String email, String phoneNumber,
                                      final OnParseOperationListener listener) {
        boolean success = true;

        // Get user info
        final User user = ParseSession.getCurrentUser(context);
        if (user == null) {
            success = false;
        }

        else {
            // Create internal callback
            OnParseOperationListener internalListener = new OnParseOperationListener() {
                @Override
                public void onSuccess(Object data) {
                    // Save new configuration
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null)
                                listener.onSuccess(user.getObjectId());
                            else
                                listener.onFailed(e.getMessage());
                        }
                    });
                }

                @Override
                public void onFailed(String error) {
                    listener.onFailed(error);
                }
            };

            // Set user name to this phone number
            user.setUsername(phoneNumber);

            // Set user info
            UserInfo userInfo = user.getUserInfo();
            if (userInfo != null && userInfo.isDataAvailable()) {
                // Update this
                updateUserInfo(userInfo, firstName, lastName, email, internalListener);
            }
            else {
                // Create New
                userInfo = createUserInfo(firstName, lastName, email);
                user.setUserInfo(userInfo);

                // Save new configuration
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null)
                            listener.onSuccess(user.getObjectId());
                        else
                            listener.onFailed(e.getMessage());
                    }
                });
            }
        }

        return success;
    }

    /**
     *  Create New User Info
     *
     * @param firstName
     * @param lastName
     * @param email
     *
     * @return
     */
    public static UserInfo createUserInfo(String firstName, String lastName, String email) {
        UserInfo userInfo = new UserInfo();
        userInfo.setFirstName(firstName);
        userInfo.setLastName(lastName);
        userInfo.setEmail(email);
        return userInfo;
    }

    /**
     *  Update User Info
     *
     * @param firstName
     * @param lastName
     * @param email
     *
     * @return
     */
    public static void updateUserInfo(final UserInfo userInfo, String firstName, String lastName, String email,
                                                     final OnParseOperationListener listener) {
        userInfo.setFirstName(firstName);
        userInfo.setLastName(lastName);
        userInfo.setEmail(email);
        userInfo.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                    listener.onSuccess(userInfo.getObjectId());
                else
                    listener.onFailed(e.getMessage());
            }
        });
    }


    /**
     * Create New Card and Save
     *
     * @param cardNumber
     * @param expireYear
     * @param expireMonth
     * @param cvc
     */
    public static void createNewCard(Context context, String cardNumber, String expireYear, String expireMonth, String cvc,
                                                                final OnParseOperationListener listener) {
        User user = ParseSession.getCurrentUser(context);
        if (user == null) {
            listener.onFailed("Error User");
            return;
        }

        // Create new card
        final CreditCard card = new CreditCard(user.getObjectId(), cardNumber, expireYear, expireMonth, cvc);
        card.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                    listener.onSuccess(card.getObjectId());
                else
                    listener.onFailed(e.getMessage());
            }
        });

    }

}
