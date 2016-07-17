package com.example.hp.thetacab;

import java.util.ArrayList;

/**
 * Created by hp on 6/15/2016.
 */
public class PathSegment {
    private Location startLocation;
    private Location endLocation;
    private ArrayList polyline;

    PathSegment(){

    }
    PathSegment(Location startLocation, Location endLocation, ArrayList polyline){
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.polyline = polyline;
    }

    void setStartLocation(Location startLocation){
        this.startLocation=startLocation;
    }

    void setEndLocation(Location endLocation){
        this.endLocation=endLocation;
    }

    void setPolyline(ArrayList polyline){
        this.polyline=polyline;
    }

    Location getStartLocation(){
        return startLocation;
    }

    Location getEndLocation(){
        return endLocation;
    }

    ArrayList getPolyline(){
        return polyline;
    }


}
