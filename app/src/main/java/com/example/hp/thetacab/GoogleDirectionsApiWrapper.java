package com.example.hp.thetacab;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Created by hp on 6/20/2016.
 */
public class GoogleDirectionsApiWrapper {
    ArrayList<OnDirectionsRetrievedListener> listeners;
    ArrayList<OnCabTypeChangedListener> cabtypeListeners;

    boolean animateMapToShowFullPath;
    String API_KEY;
    String source;
    String destination;
    GoogleMap map;
    String requestURL;
    String baseURL;
    Polyline pathPolyline;
    boolean removePath;
    TextView textView;
    String etaString;
    String fairEstimateString;
    String totalDistanceString;
    String totalTimeString;
    int totalDitanceInMeters;
    int totalTimeInSeconds;
    TextView fairEstimateTV;
    TextView etaTV;
    int cabType;
    int bikeBaseRate;
    int bikeDistanceCoefficient;
    int bikeTimeCoefficient;
    int carBaseRate;
    int carDistanceCoefficent;
    int carTimeCoefficient;

    String bikeBaseRateKey = "bikeBaseRate";
    String bikeDistanceCoefficientKey = "bikeDistanceCoefficient";
    String bikeTimeCoefficientKey = "bikeTimeCoefficient";
    String carBaseRateKey = "carBaseRate";
    String carDistanceCoefficientKey = "carDistanceCoefficient";
    String carTimeCoefficientKey = "carTimeCoefficient";

    public GoogleDirectionsApiWrapper(String API_KEY){
        listeners = new ArrayList<>();
        cabtypeListeners = new ArrayList<>();
        this.API_KEY = API_KEY;
        baseURL = "https://maps.googleapis.com/maps/api/directions/json?"+"key="+API_KEY;
        requestURL = baseURL;
        animateMapToShowFullPath = true;
        removePath = false;
        cabType=1;
        listenForFairCalculationParams();
    }

    public GoogleDirectionsApiWrapper animateMapToShowFullPath(boolean animate){
        animateMapToShowFullPath = animate;
        return this;
    }

    public GoogleDirectionsApiWrapper setTextView(TextView textView){
        this.textView = textView;
        return this;
    }

    public interface OnDirectionsRetrievedListener{
        void onDirectionsRetrieved(CompletePathData completePathData);
    }

    public interface OnCabTypeChangedListener{
        void onCabTypeChanged(int cabType);
    }

    public void setCabType(int cabType){
        this.cabType = cabType;
        for(ListIterator<OnCabTypeChangedListener> it = cabtypeListeners.listIterator(); it.hasNext(); ){
            OnCabTypeChangedListener aListener = it.next();
            aListener.onCabTypeChanged(cabType);
        }
    }



    public void setEtaTV(TextView tv){
        this.etaTV = tv;
    }

    public void setFairEstimateTV(TextView tv){
        this.fairEstimateTV = tv;
    }
    public void setOnDirectionsRetrivedListener(OnDirectionsRetrievedListener onDirectionsRetrievedListener){
        this.listeners.add(onDirectionsRetrievedListener);
    }
    public void setOnCabTypeChangedListener(OnCabTypeChangedListener onCabTypeChangedListener){
        this.cabtypeListeners.add(onCabTypeChangedListener);
    }
    public void changeEstimateWhenCabTypeChanged(){
        setOnCabTypeChangedListener(new OnCabTypeChangedListener() {
            @Override
            public void onCabTypeChanged(int cabType) {
                fairEstimateTV.setText(
                        totalDistanceString+
                                " in "+
                                totalTimeString+
                                " will cost Rs. "+
                                getFairEstimate(totalDitanceInMeters,totalTimeInSeconds)

                );
            }
        });

    }

    public GoogleDirectionsApiWrapper from(String from){
        source = "origin=" + from.replaceAll(" ","+");
        return this;
    }

    public GoogleDirectionsApiWrapper from(LatLng from){
        source = "origin=" + from.latitude + "," + from.longitude;
        return this;
    }

    public GoogleDirectionsApiWrapper to(String to){
        destination = "destination=" + to.replaceAll(" ","+");
        return this;
    }

    public GoogleDirectionsApiWrapper to(LatLng to){
        destination = "destination=" + to.latitude + "," + to.longitude;
        return this;
    }

    public GoogleDirectionsApiWrapper retreiveDirections(){
        if(source!=null && destination!=null) {
            requestURL = baseURL + "&" + source + "&" + destination;
            RequestDirectionsTask requestDirectionsTask = new RequestDirectionsTask();
            requestDirectionsTask.execute(requestURL);
        }else if(source == null){

            try {
                throw new Exception("Source should not be Null: this method must be called after the from() and to() methods");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else{

            try {
                throw new Exception("destination should not be null: this method must be called after the from() and to() methods");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return this;
    }


    public void removePath(){
        if(pathPolyline==null) {
            removePath = true;
        }else{
            pathPolyline.remove();
        }

    }

    public void removePathThatIsVisible(){
        if(pathPolyline!=null){
            pathPolyline.remove();
        }
    }

    public GoogleDirectionsApiWrapper setMap(GoogleMap map){
        this.map = map;
        return this;
    }

    private void listenForFairCalculationParams(){
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(bikeBaseRateKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    bikeBaseRate = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(bikeDistanceCoefficientKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    bikeDistanceCoefficient = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(bikeTimeCoefficientKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    bikeTimeCoefficient = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(carBaseRateKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    carBaseRate = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(carDistanceCoefficientKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    carDistanceCoefficent =Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(carTimeCoefficientKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    carTimeCoefficient = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    int getFairEstimate(int totalDistanceInM, int etaInSeconds){
        double distanceInKm = ((double)totalDistanceInM)/((double) 1000);
        int KMs = (int) (distanceInKm+0.5);
        double etaInMinutes = ((double) etaInSeconds)/((double)60);
        int Mins = (int) (etaInMinutes + 0.5);

        if(cabType == 0){
            if(KMs<=5){
                return (5*bikeDistanceCoefficient)+(Mins*bikeTimeCoefficient)+bikeBaseRate;
            }else if(KMs>=20){
                return (20*bikeDistanceCoefficient)+(Mins*bikeTimeCoefficient)+bikeBaseRate;
            }else {
                return (KMs*bikeDistanceCoefficient)+(Mins*bikeTimeCoefficient)+bikeBaseRate;
            }
        }else if(cabType ==1){
            return (KMs*carDistanceCoefficent)+(Mins*carTimeCoefficient)+carBaseRate;
        }else {
            return -1;
        }
    }

    String getFairEstimateString(){
        return fairEstimateString;
    }

    String getEtaString(){
        return etaString;
    }

    public void drawPathOnMap(){
        setOnDirectionsRetrivedListener(new OnDirectionsRetrievedListener() {
            @Override
            public void onDirectionsRetrieved(CompletePathData completePathData) {
                listeners.clear();
                totalDistanceString = completePathData.getTotalDistance();
                totalDitanceInMeters = completePathData.getTotal_distance_in_m();
                totalTimeInSeconds = completePathData.getEta_in_seconds();
                totalTimeString = completePathData.getEta();
                fairEstimateString = completePathData.getTotalDistance() +
                        " in " +
                        completePathData.getEta() +
                        " will cost Rs. " +
                        getFairEstimate(
                                completePathData.getTotal_distance_in_m(), completePathData.getEta_in_seconds()
                        );
                etaString = completePathData.getTotalDistance() + " in " + completePathData.getEta();

                if (etaTV != null) {
                    etaTV.setText(etaString);
                }
                if (fairEstimateTV != null) {
                    fairEstimateTV.setText(fairEstimateString);
                }
                if (completePathData.getStartLocation() != null) {
                    Log.v("Path Data", " Source: " + completePathData.getStartLocation().toString() + " Destination: " + completePathData.getEndLocation().toString());
                }

                // Instantiates a new Polyline object and adds points to define a rectangle
                ArrayList<PathSegment> resultRoute = completePathData.getPathSegments();
                PolylineOptions pathOptions = new PolylineOptions();
                if (resultRoute != null) {
                    for (int i = 0; i < resultRoute.size(); i++) {
                        PathSegment currPathSegment = resultRoute.get(i);
                        pathOptions.add(new LatLng(currPathSegment.getStartLocation().getLatitude(), currPathSegment.getStartLocation().getLongitude()));
                        ArrayList<Location> polyline = currPathSegment.getPolyline();
                        for (int j = 0; j < polyline.size(); j++) {
                            pathOptions.add(new LatLng(polyline.get(j).getLatitude(), polyline.get(j).getLongitude()));
                        }
                        pathOptions.add(new LatLng(currPathSegment.getEndLocation().getLatitude(), currPathSegment.getEndLocation().getLongitude()));
                    }
                }
                // Get back the mutable Polyline
                pathPolyline = map.addPolyline(pathOptions.color(Color.parseColor("#dd1D3643")).width((float) 20.0).geodesic(true).zIndex((float) 20.0));
                // Create a LatLngBounds that includes Australia.
                if (removePath) {
                    pathPolyline.remove();
                }
                if (animateMapToShowFullPath && !removePath) {
                    if (completePathData.getNorthEastLatLng() != null && completePathData.getSouthWestLatLng() != null) {

                        LatLngBounds pathBounds = new LatLngBounds(
                                completePathData.getSouthWestLatLng(), completePathData.getNorthEastLatLng());

                        // Set the camera to the greatest possible zoom level that includes the
                        // bounds
                        LatLng middleLatLng = new LatLng(
                                (
                                        completePathData.getNorthEastLatLng().latitude
                                                +
                                                completePathData.getSouthWestLatLng().latitude
                                ) / 2,
                                (
                                        completePathData.getNorthEastLatLng().longitude
                                                +
                                                completePathData.getSouthWestLatLng().longitude

                                ) / 2);
                        LatLng currentMapLatLng = map.getCameraPosition().target;
                        float currTilt = map.getCameraPosition().tilt;
                        float currZoom = map.getCameraPosition().zoom;

                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(pathBounds, 60));
                        LatLng cameraLatLng = map.getCameraPosition().target;
                        float zoomLevel = map.getCameraPosition().zoom;
                        zoomLevel -= 1.5;

                        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder().
                                        tilt(currTilt).
                                        zoom(currZoom).
                                        target(currentMapLatLng).
                                        build()
                        ));

                        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder().
                                        tilt(currTilt).
                                        zoom(zoomLevel).
                                        target(middleLatLng).
                                        build()
                        ));
                    }
                    Log.v("DirectionPath", ":::: " + completePathData.toString());
                    removePath = false;

                }

            }
        });
    }

    public void refreshPath(String source){

    }

    private static CompletePathData getPathFromURL(String link){
        String responseString;
        CompletePathData completePathData = new CompletePathData();
        ArrayList<PathSegment> routeData=new ArrayList<>();
        try {
            URL url=new URL(link);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());

            //now we have the string
            responseString = convertStreamToString(in);
            httpURLConnection.disconnect();


        } catch (MalformedURLException e) {
            responseString="MalformedURLError";
            e.printStackTrace();
        } catch (IOException e) {
            responseString="IOException";
            e.printStackTrace();
        }

        try {
            JSONObject resoponseObject = new JSONObject(responseString);
            JSONArray routesArray = resoponseObject.getJSONArray("routes");
            JSONObject routeObject = routesArray.getJSONObject(0);
            JSONArray legsArray = routeObject.getJSONArray("legs");
            JSONObject legObject = legsArray.getJSONObject(0);
            JSONArray stepsArray = legObject.getJSONArray("steps");
            for(int i=0;i<stepsArray.length();i++){
                Location startLocation;
                Location endLocation;
                ArrayList polyline;
                JSONObject stepObject = stepsArray.getJSONObject(i);
                JSONObject startLocationObject = stepObject.getJSONObject("start_location");
                JSONObject endLocationObject = stepObject.getJSONObject("end_location");
                JSONObject polylineObject = stepObject.getJSONObject("polyline");
                startLocation=new Location(startLocationObject.getDouble("lat"),startLocationObject.getDouble("lng"));
                endLocation=new Location(endLocationObject.getDouble("lat"),endLocationObject.getDouble("lng"));
                polyline=PolylineDecoder.decodePoly(polylineObject.getString("points"));
                routeData.add(new PathSegment(startLocation,endLocation,polyline));
            }
            JSONObject boundObject=routeObject.getJSONObject("bounds");
            JSONObject northEastObject=boundObject.getJSONObject("northeast");
            JSONObject southWestObject=boundObject.getJSONObject("southwest");
            Double northEastLat=northEastObject.getDouble("lat");
            Double northEastLong=northEastObject.getDouble("lng");
            Double southWestLat=southWestObject.getDouble("lat");
            Double southWestLong=southWestObject.getDouble("lng");

            LatLng northEastLatLng = new LatLng(northEastLat,northEastLong);
            LatLng southWestLatLng = new LatLng(southWestLat,southWestLong);

            JSONObject distanceObject=legObject.getJSONObject("distance");
            String totalDistance = distanceObject.getString("text");
            int totalDistanceInM = distanceObject.getInt("value");

            JSONObject durationObject=legObject.getJSONObject("duration");
            String totalDuration = durationObject.getString("text");
            int etaInSeconds = durationObject.getInt("value");

            JSONObject startLocationObject=legObject.getJSONObject("start_location");
            JSONObject endLocationObject=legObject.getJSONObject("end_location");

            LatLng startLatLng = new LatLng(startLocationObject.getDouble("lat"),startLocationObject.getDouble("lng"));
            LatLng endLatLng = new LatLng(endLocationObject.getDouble("lat"),endLocationObject.getDouble("lng"));

            completePathData= new CompletePathData(routeData,totalDistance,totalDuration,startLatLng,endLatLng,northEastLatLng,southWestLatLng,totalDistanceInM,etaInSeconds);



        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v("PathString"," "+responseString);
        return completePathData;
    }

    private static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private class RequestDirectionsTask extends AsyncTask<String, String, CompletePathData> {

        @Override
        protected CompletePathData doInBackground(String... webAddresses) {
            return getPathFromURL(webAddresses[0]);
        }

        @Override
        protected void onPostExecute(CompletePathData result) {
            super.onPostExecute(result);

//            List<String> s = new ArrayList<String>();
//
//            for(String a : args)
//                s.add(a);
//
//            ListIterator<String> it = s.listIterator();
//            if(it.hasNext()) {
//                String item = it.next();
//            }


            for(ListIterator<OnDirectionsRetrievedListener> it = listeners.listIterator(); it.hasNext(); ){
                OnDirectionsRetrievedListener aListener = it.next();
                aListener.onDirectionsRetrieved(result);
            }

        }
    }



}
