package com.example.hp.thetacab;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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

/**
 * Created by hp on 6/21/2016.
 */
public class GoogleReverseGeocodingApiWrapper {
    ArrayList<OnAddressRetrievedListener> listeners;

    private String API_KEY;
    private String requestURL;
    private String resultingAddress;

    public GoogleReverseGeocodingApiWrapper(String API_KEY){
        this.listeners = new ArrayList<>();
        this.API_KEY = API_KEY;
        requestURL = "https://maps.googleapis.com/maps/api/geocode/json?"+"key="+API_KEY;
    }

    public interface OnAddressRetrievedListener{
        void onAddressRetrieved(String resultingAddress);
    }

    public void setOnAddressRetrievedListener(OnAddressRetrievedListener onAddressRetrievedListener){
        this.listeners.add(onAddressRetrievedListener);
    }

    public GoogleReverseGeocodingApiWrapper setLatLng(LatLng latLng){
        requestURL = requestURL + "&" +"latlng="+ latLng.latitude + "," + latLng.longitude;
        return this;
    }

    public GoogleReverseGeocodingApiWrapper requestAddress(){
        RequestDirectionsTask requestDirectionsTask = new RequestDirectionsTask();
        requestDirectionsTask.execute(requestURL);
        return this;
    }

    private class RequestDirectionsTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... webAddresses) {
            return getPathFromURL(webAddresses[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            resultingAddress = result;
            for (OnAddressRetrievedListener listener: listeners){
                listener.onAddressRetrieved(result);
            }

        }
    }

    private static String getPathFromURL(String link){
        String responseString;
        String customAddressString = "";
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

        //Log.v("PathString"," "+responseString);
        try {
            JSONObject addressesJSON = new JSONObject(responseString);
            JSONArray addressesJSONArray = addressesJSON.getJSONArray("results");
            JSONObject addressJSONObject = addressesJSONArray.getJSONObject(0);
            String formattedAddress = addressJSONObject.getString("formatted_address");
            JSONArray addressComponentsJSONArray = addressJSONObject.getJSONArray("address_components");
            for(int i=0;i<addressComponentsJSONArray.length();i++){
                JSONObject currObj = addressComponentsJSONArray.getJSONObject(i);
                String longName = currObj.getString("long_name");
                String[] longNameParts = longName.split(" ");
                boolean anyPartOfLongNameIsInTheCurrentAddress = false;
                for(int j=0;j<longNameParts.length;j++){
                    if(customAddressString.contains(longNameParts[j])){
                        anyPartOfLongNameIsInTheCurrentAddress=true;
                        break;
                    }
                }
                if(!anyPartOfLongNameIsInTheCurrentAddress) {
                    customAddressString = customAddressString + " " + longName;
                }

            }
            Log.v("Formatted Address", " "+formattedAddress);
            Log.v("Custom Address", " "+ customAddressString);
            //Log.v("PathString"," "+addressJSONObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return customAddressString;
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
