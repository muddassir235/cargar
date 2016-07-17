package com.example.hp.thetacab;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CabTrackerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    TextView cabLatLongTV;

    //This is the marker which is always at the current known location of the driver
    Marker locationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cab_tracker);

        // Obtain the SupportMapFragment and get notified when
        // the map is ready to be used.
        initializeViews();

        //get database reference! note: by default the reference starts at the root of our reatime database (json tree)
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        //add a ValueEventListener to the place where the driver's location is being updated.
        db.child("locationDriver").child("location").addValueEventListener(new ValueEventListener() {

            //we get a snapshot of data stored in the node "locationDriver"
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //verifying that the data snapshot is not null
                if(dataSnapshot!=null){

                    //verifying that the value of the dataSnapshot is not null
                    //( This will be the case if we have disabled tracking and deleted the location of the driver )
                    if(dataSnapshot.getValue()!=null) {

                        //set the value of the Text View to show the updated location
                        cabLatLongTV.setText((CharSequence) dataSnapshot.getValue());

                        //getting the string where the location data is present
                        String cabLocationString = (String) dataSnapshot.getValue();

                        //splitting the string into lat and long
                        String[] latLongsString = cabLocationString.split(" ");

                        //casting the lat and long to the appropriate data-type
                        double cabLat = Double.valueOf(latLongsString[0]);
                        double cabLong = Double.valueOf(latLongsString[1]);

                        Log.v("Location", " " + cabLat + " " + cabLong);

                        //making a new LatLng object to set the map's camera and update the location of the cab marker
                        LatLng latLngCab = new LatLng(cabLat, cabLong);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCab, (float) 10));
                        locationMarker.setPosition(latLngCab);

                    }else {

                        //if location tracking is disabled show appropriate message
                        Log.v("Location"," value of location is null");
                        cabLatLongTV.setText(" Location tracking has been disabled");
                    }
                }else {

                    //if data snapshot is null show appropriate message
                    Log.v("Location"," dataSnapshot of location is null");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    void initializeViews(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        cabLatLongTV = (TextView) findViewById(R.id.cab_lat_long_tv);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        locationMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").icon(BitmapDescriptorFactory.fromResource(R.mipmap.cab_marker)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
