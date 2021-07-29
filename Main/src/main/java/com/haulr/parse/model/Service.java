package com.haulr.parse.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * @description     Service Object
 * @author          Adrian
 */
@ParseClassName("Service")
public class Service extends ParseObject {
    // Table Name
    public final static String SERVICE_TABLE_NAME = "Service";

    // Fields
    public final static String FIELD_CUSTOMER = "customer";
    public final static String FIELD_DRIVER = "driver";
    public final static String FIELD_SERVICE_INFO = "serviceInfo";
    public final static String FIELD_STATUS = "status";
    public final static String FIELD_RATING = "rating";

    public Service() {
        super();
    }

    public Service(User customer, ServiceInfo serviceInfo) {
        setCustomer(customer);
        setServiceInfo(serviceInfo);

        setStatus(ServiceStatus.PENDING);
    }

    // Getter
    public User getCustomer() {
        return (User) getParseUser(FIELD_CUSTOMER);
    }
    public User getDriver() {
        return (User) getParseUser(FIELD_DRIVER);
    }
    public ServiceInfo getServiceInfo() {
        return (ServiceInfo) getParseObject(FIELD_SERVICE_INFO);
    }
    public ServiceStatus getStatus() {
        String statusStr = getString(FIELD_STATUS);

        ServiceStatus status = ServiceStatus.PENDING;

        if (statusStr.equals(ServiceStatus.LEAVE_TO_PICKUP.toString()))
            status = ServiceStatus.LEAVE_TO_PICKUP;
        else if (statusStr.equals(ServiceStatus.LEAVE_TO_DROPOFF.toString()))
            status = ServiceStatus.LEAVE_TO_DROPOFF;
        else if (statusStr.equals(ServiceStatus.ARRIVED_AT_PICKUP.toString()))
            status = ServiceStatus.ARRIVED_AT_PICKUP;
        else if (statusStr.equals(ServiceStatus.ARRIVED_AT_DROPOFF.toString()))
            status = ServiceStatus.ARRIVED_AT_DROPOFF;
        else if (statusStr.equals(ServiceStatus.COMPLETED.toString()))
            status = ServiceStatus.COMPLETED;

        return status;
    }

    // Setter
    public void setCustomer(User value) {
        put(FIELD_CUSTOMER, value);
    }
    public void setDriver(User value) {
        put(FIELD_DRIVER, value);
    }
    public void setServiceInfo(ServiceInfo value) {
        put(FIELD_SERVICE_INFO, value);
    }
    public void setStatus(ServiceStatus status) { put(FIELD_STATUS, status.toString()); }
    public void setSetRating(float value) { put(FIELD_RATING, value); }
}
