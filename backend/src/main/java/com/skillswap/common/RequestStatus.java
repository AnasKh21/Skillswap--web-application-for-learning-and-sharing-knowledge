package com.skillswap.common;

public enum RequestStatus {
    PENDING,    // waiting for the receiver to respond
    ACCEPTED,   // receiver accepted the request
    DECLINED,   // receiver declined the request
    CANCELLED   // initiator cancelled the request
}
