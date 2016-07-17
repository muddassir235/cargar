package com.example.hp.thetacab;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hp on 7/11/2016.
 */
public class CabOrder {
    private String mId;
    private String mPassengerId;
    private String mDriverId;
    private LatLng mDriverLatLng;
    private LatLng mPickUpPointLatLng;
    private LatLng mDestinationLatLng;
    private String mPickUpPointAddress;
    private String mDestinationPointAddress;
    private int mCabType;

    private boolean mReachedDatabase;
    private boolean mReachedServer;
    private boolean mPassengerDriverMatched;
    private int mNumberOfTimesRejected;
    private String[] mIDsRejectedDrivers;


    private static final int CAB_TYPE_BIKE = 0;
    private static final int CAB_TYPE_CAR = 1;

    public CabOrder(){

    }
}
