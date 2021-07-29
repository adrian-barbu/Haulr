package com.haulr.parse.model;

/**
 * @description Define Service Status
 * @author      Adrian
 */
public enum ServiceStatus {
    PENDING,            // The request is pending
    LEAVE_TO_PICKUP,    // The driver is selected and leave to Pickup Address
    ARRIVED_AT_PICKUP,  // The driver arrived at Pickup Address
    LEAVE_TO_DROPOFF,   // The driver picked up, leave to DropOff Address
    ARRIVED_AT_DROPOFF, // The driver arrived at DropOff Address
    COMPLETED          // The haul is completed
}
