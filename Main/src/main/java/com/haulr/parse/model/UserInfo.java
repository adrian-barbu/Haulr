package com.haulr.parse.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * @description     Parse UserInfo Object
 * @author          Adrian
 */
@ParseClassName("UserInfo")
public class UserInfo extends ParseObject {
    // Fields
    private final String FIELD_FIRST_NAME = "firstName";
    private final String FIELD_LAST_NAME = "lastName";
    private final String FIELD_EMAIL = "email";

    public UserInfo() {
        super();
    }

    // Methods
    public String getFirstName() {
        return getString(FIELD_FIRST_NAME);
    }
    public String getLastName() {
        return getString(FIELD_LAST_NAME);
    }
    public String getEmail() {
        return getString(FIELD_EMAIL);
    }


    public void setFirstName(String value) {
        put(FIELD_FIRST_NAME, value);
    }
    public void setLastName(String value) {
        put(FIELD_LAST_NAME, value);
    }
    public void setEmail(String value) {
        put(FIELD_EMAIL, value);
    }
}
