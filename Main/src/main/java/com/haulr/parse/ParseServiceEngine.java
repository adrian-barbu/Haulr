package com.haulr.parse;

import android.content.Context;
import android.location.Location;

import com.haulr.R;
import com.haulr.googleapi.MyLocation;
import com.haulr.parse.model.PlaceInfo;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

/**
 * @description     Parse Service Engine
 *                  Include most of functions related to service
 *
 * @author          Adrian
 */
public class ParseServiceEngine extends ParseEngine {

    /**
     *  Create New Service
     *
     * @param pickupInfo
     * @param dropoffInfo
     * @param distance
     * @param duration
     * @param listener
     */
    public static void createNewService(Context context, PlaceInfo pickupInfo, PlaceInfo dropoffInfo,
                                        String distance, String duration, double price, String cardNumber, String transactionID,
                                        final byte[] imageData,
                                        final OnParseOperationListener listener) {

        final User user = ParseSession.getCurrentUser(context);
        if (user == null) {
            listener.onFailed("Error User");
            return;
        }

        /*
            Create Service Info
         */
        final ServiceInfo si = new ServiceInfo(pickupInfo, dropoffInfo, distance, duration, price, cardNumber, transactionID);
        si.saveInBackground();

        if (imageData != null)  // Save Image Data
        {
            ParseFile file = new ParseFile("photo.png", imageData);
            // Upload the image into Parse Cloud
            file.saveInBackground();

            si.setItemImage(file);
            si.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){
                        listener.onSuccess(si.getObjectId());
                        final Service service = new Service(user /* this is customer */, si);
                        service.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null)
                                    listener.onSuccess(service.getObjectId());
                                else
                                    listener.onFailed(e.getMessage());
                            }
                        });
                    }
                    else listener.onFailed(e.getMessage());
                }
            });
        }
        else {
            /*
                Create New Service
             */
            final Service service = new Service(user /* this is customer */, si);
            service.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null)
                        listener.onSuccess(service.getObjectId());
                    else
                        listener.onFailed(e.getMessage());
                }
            });
        }
    }


    /**
     * Find Service Object With Service Info
     *
     * This class will be shared in several activity
     *
     * @param serviceInfo   : the info of serviceInfo table
     * @param callback      : the callback when service is got or not
     */
    public static void getService(ServiceInfo serviceInfo, GetCallback<Service> callback) {
        ParseObject pInfo = ParseObject.createWithoutData(ServiceInfo.SERVICE_INFO_TABLE_NAME, serviceInfo.getObjectId());

        ParseQuery<Service> serviceQuery = ParseQuery.getQuery(Service.SERVICE_TABLE_NAME);
        serviceQuery.whereEqualTo(Service.FIELD_SERVICE_INFO, pInfo);

        serviceQuery.getFirstInBackground(callback);
    }

    /**
     * Find Nearest Service From Current Position
     *
     * @param serviceList
     * @return
     */
    public static ServiceInfo findNearestService(List<Service> serviceList, Location myLocation) {
        ServiceInfo nearestInfo = null;

        ParseGeoPoint myPosition = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());
        double distance, nearestDist = -1;

        ServiceInfo si;
        for (Service service : serviceList) {
            try {
                si = service.getServiceInfo();
                if (si != null) {
                    distance = si.getPickupGeo().distanceInKilometersTo(myPosition);

                    if (nearestDist == -1 || distance < nearestDist) {
                        nearestDist = distance;
                        nearestInfo = si;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return nearestInfo;
    }
}
