package com.example.hp.thetacab;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by hp on 6/15/2016.
 */
public class CompletePathData {
    private ArrayList<PathSegment> pathSegments;
    private String total_distance;
    private String eta;
    private LatLng northEastLatLng;
    private LatLng southWestLatLng;
    private LatLng startLocation;
    private LatLng endLocation;
    private int total_distance_in_m;
    private int eta_in_seconds;

    CompletePathData(){
        //empty constructor
    }

    CompletePathData(ArrayList<PathSegment> pathSegments,String total_distance, String eta, LatLng startLocation, LatLng endLocation, LatLng northEastLatLng,LatLng southWestLatLng,int total_distance_in_m,int eta_in_seconds){
        this.pathSegments=pathSegments;
        this.total_distance=total_distance;
        this.eta=eta;
        this.startLocation=startLocation;
        this.endLocation=endLocation;
        this.northEastLatLng=northEastLatLng;
        this.southWestLatLng=southWestLatLng;
        this.total_distance_in_m = total_distance_in_m;
        this.eta_in_seconds = eta_in_seconds;
    }

    void setPathSegments(ArrayList<PathSegment> pathSegments){
        this.pathSegments=pathSegments;
    }
    void setTotalDistance(String total_distance){
        this.total_distance=total_distance;
    }
    void setEta(String eta){
        this.eta=eta;
    }
    void setStartLocation(LatLng startLocation){
        this.startLocation=startLocation;
    }
    void setEndLocation(LatLng endLocation){
        this.endLocation=endLocation;
    }
    void setNorthEastLatLng(LatLng northEastLatLng){
        this.northEastLatLng=northEastLatLng;
    }
    void setSouthWestLatLng(LatLng southWestLatLng){
        this.southWestLatLng=southWestLatLng;
    }

    ArrayList<PathSegment> getPathSegments(){
        return pathSegments;
    }

    String getTotalDistance(){
        return total_distance;
    }

    String getEta(){
        return eta;
    }

    LatLng getStartLocation(){
        return startLocation;
    }

    LatLng getEndLocation(){
        return endLocation;
    }

    LatLng getNorthEastLatLng(){
        return northEastLatLng;
    }
    LatLng getSouthWestLatLng(){
        return southWestLatLng;
    }

    public int getEta_in_seconds() {
        return eta_in_seconds;
    }

    public int getTotal_distance_in_m() {
        return total_distance_in_m;
    }
}
