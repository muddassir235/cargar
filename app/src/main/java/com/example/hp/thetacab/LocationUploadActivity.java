package com.example.hp.thetacab;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.*;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.text.TextDirectionHeuristicCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.LocationSource;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LocationUploadActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    TextView currentLocationTV;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    //Boolean for getting the status of whether location tracking is enabled or not
    boolean trackingEnabled=true;

    //google play services api client
    GoogleApiClient mGoogleApiClient;

    //field for storing current location so it it can be globally accessed in the class
    Location mCurrentLocation;

    //should we request regular location updates
    boolean mRequestingLocationUpdates = true;

    //Location request defines the parameter for the request (e.g. the frequency with which we want
    //                                                          to recieve updates)
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_upload);
        intializeViews();
        getLocationPermissions();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();


        }

        //getting a reference to the floating action button defined in xml
        final FloatingActionButton floatingActionButton=(FloatingActionButton) findViewById(R.id.fab_stop_tracking);
        assert floatingActionButton != null;

        //addign an onClickListener to the FloatingActionButton
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if location tracking is enable and the FloatingActionButton is clicked
                if(trackingEnabled){

                    //disconnest the google play services client
                    //
                    mGoogleApiClient.disconnect();
                    DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference();
                    dbRef.child("locationDriver").child("location").setValue(null);
                    floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#44ff44")));
                    currentLocationTV.setText("Stopped Location Tracking !");
                    trackingEnabled=false;
                }else{
                    mGoogleApiClient.connect();
                    floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff4444")));
                    currentLocationTV.setText("Starting Location Tracking ...");
                    trackingEnabled=true;
                }
            }
        });
    }

    void intializeViews() {
        currentLocationTV = (TextView) findViewById(R.id.current_location_tv);
    }

    void getLocationPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_BLUETOOTH is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }


            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }


    protected void startLocationUpdates() {
        createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiClient.connect();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }




    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            Log.v("Location"," "+location.getLatitude()+" "+location.getLongitude());
            currentLocationTV.setText("lat: "+location.getLatitude()+"   "+"lng: "+location.getLongitude());
            DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference();
            com.example.hp.thetacab.Location currentLocation= new com.example.hp.thetacab.Location(location.getLatitude(),location.getLongitude());
            dbRef.child("locationDriver").child("location").setValue(""+location.getLatitude()+" "+location.getLongitude());
        }


    }
}
