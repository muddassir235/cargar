package com.example.hp.thetacab;

import android.location.*;
import android.location.Location;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by hp on 7/13/2016.
 */
public class Utils {

    public static int getDistanceInMetersFromLatLngData(ArrayList<LatLng> data){
        int meters=0;
        int length = data.size();
        int i=0;
        for(LatLng latLng:data){
            if(i == (length-1)) {
                LatLng a = latLng;
                LatLng b = data.get(i + 1);
                float[] results = new float[3];
                Location.
                        distanceBetween(
                                a.latitude,
                                b.longitude,
                                b.latitude,
                                b.longitude,
                                results
                        );
                meters += results[0];
            }
            i++;
        }
        return meters;
    }
}
