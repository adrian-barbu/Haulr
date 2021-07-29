package com.haulr.parse.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * @description     User Object (customize of User)
 * @author          Adrian
 */
@ParseClassName("_User")
public class User extends ParseUser {
    // Fields
    private final String FIELD_PHONE_NUMBER = "phoneNumber";
    private final String FIELD_VERIFIED = "verified";
    private final String FIELD_VERIFY_CODE = "verifyCode";
    private final String FIELD_VERIFY_CODE_SENT = "verifyCodeSent";
    private final String FIELD_ALLOW_DRIVER = "allowDriver";
    private final String FIELD_AS_DRIVER = "asDriver";
    private final String FIELD_IS_ONLINE = "isOnline";
    private final String FIELD_USER_INFO = "userInfo";

    public User() {
        super();
    }

    /**
     * This constructor will be called when new user is registered.
     *
     * @param phoneNumber
     * @param verifyCode
     */
    public User(String phoneNumber, String verifyCode) {
        setPhoneNumber(phoneNumber);
        setUsername(phoneNumber);
        setVerifyCode(verifyCode);
        setVerified(false);
        setVerifyCodeSent(false);
    }

    // Getter
    public String getPhoneNumber() {
        return getString(FIELD_PHONE_NUMBER);
    }
    public boolean isVerified() {
        return getBoolean(FIELD_VERIFIED);
    }
    public String getVerifyCode() {
        return getString(FIELD_VERIFY_CODE);
    }
    public boolean isVerifyCodeSent() {
        return getBoolean(FIELD_VERIFY_CODE_SENT);
    }
    public boolean allowDriver() {
        return getBoolean(FIELD_ALLOW_DRIVER);
    }
    public boolean asDriver() {
        return getBoolean(FIELD_AS_DRIVER);
    }
    public boolean isOnline() {
        return getBoolean(FIELD_IS_ONLINE);
    }
    public UserInfo getUserInfo() { return (UserInfo)getParseObject(FIELD_USER_INFO); }

    // Setter
    public void setPhoneNumber(String value) {
        put(FIELD_PHONE_NUMBER, value);
    }
    public void setVerified(boolean value) {
        put(FIELD_VERIFIED, value);
    }
    public void setVerifyCode(String value) {
        put(FIELD_VERIFY_CODE, value);
    }
    public void setVerifyCodeSent(boolean value) {
        put(FIELD_VERIFY_CODE_SENT, value);
    }
    public void setAsDriver(boolean value) {
        put(FIELD_AS_DRIVER, value);
    }
    public void setOnline(boolean value) {
        put(FIELD_IS_ONLINE, value);
    }
    public void setUserInfo(UserInfo info) {
        put(FIELD_USER_INFO, info);
    }

}
