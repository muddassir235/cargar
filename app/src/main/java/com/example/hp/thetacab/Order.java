package com.example.hp.thetacab;

/**
 * Created by gul on 6/28/16.
 */
public class Order {
    public String source;
    public String destination;
    public String sourceLat;
    public String sourceLong;
    public String destLat;
    public String destLong;
    public int cabType;
    public Order(){}
    public Order(String source, String destination,String sourceLat,String sourceLong,String destLat,String destLong ,int cabType){
        this.source=source;
        this.destination=destination;
        this.cabType=cabType;
        this.sourceLat=sourceLat;
        this.sourceLong=sourceLong;
        this.destLat=destLat;
        this.destLong=destLong;
    }
}
