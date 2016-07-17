package com.example.hp.thetacab.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v8.renderscript.Double2;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.thetacab.Animations;
import com.example.hp.thetacab.Constants;
import com.example.hp.thetacab.DriverLocation;
import com.example.hp.thetacab.GoogleDirectionsApiWrapper;
import com.example.hp.thetacab.Order;
import com.example.hp.thetacab.R;
import com.example.hp.thetacab.SendNotif;
import com.example.hp.thetacab.SystemBarTintManager;
import com.example.hp.thetacab.tApplication;
import com.github.hujiaweibujidao.wava.Techniques;
import com.github.hujiaweibujidao.wava.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Driver;
import java.util.ArrayList;

import eightbitlab.com.blurview.BlurView;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "DriverMapActivity" ;


    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private GoogleDirectionsApiWrapper googleDirectionsApiWrapper;

    boolean orderRecieved;
    //////gullu booleans///////////////
    boolean connected;
    boolean ifArriving;
    //////////////////////////////////


    private LocationRequest mLocationRequest;

    private Marker cabMarker;


    private GoogleMap mMap;
    private boolean isOnline;
    private CardView driverCard;
    private RelativeLayout onlineStatusLayout;
    private TextView onlineTextView;
    private Switch onlineSwitch;
    private LinearLayout onlineStatusBarDriverCard;
    private BlurView callBackgroundBV;
    private AVLoadingIndicatorView callCenterAnimView;
    private TextView passengerCallCountdownTV;
    private ProgressBar passengerCallCountdownPB;
    private Button acceptCallButton;
    private Button rejectCallButton;
    private LinearLayout acceptRejectLayout;
    private TextView passengerLoactionTV;
    private Marker passengerMarker;
    private Marker destinationMarker;
    private TextView passengerNameTV;
    private ImageView passengerIV;
    private CardView passengerCard;
    private Button cabArrivedButton;
    private Button passengerBoardedButton;
    private Button startTripButton;
    private Button endTripButton;
    private TextView destinationTV;
    private ImageButton mOpenDrawerButton;


    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    private Order currOrder;
    private String orderCustomerId;
    private String driverName;
    private String instanceId;
    //should we request regular location updates
    boolean mRequestingLocationUpdates = true;

    private boolean afterTripCancelled;
    private boolean tripCancelled;
    private boolean inTrip;

    // data of path that is bieng taken by current trip
    private ArrayList<LatLng> mTripPathData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        afterTripCancelled = sharedPref.getBoolean("AfterTripCancelled",false);
        tripCancelled = false;
        isOnline = afterTripCancelled;
        connected=false;
        ifArriving=false;
        mTripPathData=new ArrayList<LatLng>();

        FirebaseDatabase.getInstance().getReference().child("IsTripOngoingStatus").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    inTrip = (Boolean) dataSnapshot.getValue();
                }else{
                    inTrip = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("notif");
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("AfterTripCancelled", false);
        editor.commit();

        setupFirebaseAuthentication();


        orderRecieved = false;
        FirebaseDatabase.getInstance().getReference().
                child("DriverLocation").
                child(getUid()).
                removeValue();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setupGoogleLocationsAPI();
        linkViews();

        googleDirectionsApiWrapper = new GoogleDirectionsApiWrapper(getResources().getString(R.string.google_directions_api_key));

        onlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOnline = isChecked;

                if (isChecked) {
                    onlineStatusLayout.setBackgroundColor(Color.parseColor("#00C77C"));
                    onlineTextView.setText("Online");
                    onlineTextView.setTextColor(Color.parseColor("#ffffff"));
                    onlineStatusBarDriverCard.setBackgroundColor(Color.parseColor("#00C77C"));
                    if(connected) {
                        startLocationUpdates();
                    }else{
                        onlineSwitch.setChecked(false);
                        Toast.makeText(DriverMapActivity.this,"You are not connected, Please wait!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    onlineStatusLayout.setBackgroundColor(Color.parseColor("#E42861"));
                    onlineTextView.setText("Offline");
                    onlineTextView.setTextColor(Color.parseColor("#aaffffff"));
                    onlineStatusBarDriverCard.setBackgroundColor(Color.parseColor("#E42861"));
                    stopLocationUpdates();
                    FirebaseDatabase.getInstance().getReference().
                            child("DriverLocation").
                            child(getUid()).
                            removeValue();
                }
            }
        });
        setStatusBarTranslucent(true);
        getLocationPermissions();
        setAllOnClickListeners();
        attachCallListener();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(inTrip){
            loadTripPathData();
        }
    }

    void setOnClickListenerOnOpenDrawerButton(){
        mOpenDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });
    }

    void loadTripPathData(){
        mTripPathData = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("TaxiCustomerMatched").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    orderCustomerId = (String) dataSnapshot.getValue();
                    FirebaseDatabase.getInstance().getReference().child("TripPath").child(orderCustomerId+getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                if(dataSnapshot.hasChildren()){
                                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                        DriverLocation location = snapshot.getValue(DriverLocation.class);
                                        LatLng latLng = new LatLng(
                                                Double.valueOf(location.Lat),
                                                Double.valueOf(location.Long)
                                        );
                                        mTripPathData.add(latLng);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void setOnClickListenerOnStartTripButton(){
        startTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send notification to passenger that the trip has started.
                inTrip = true;
                FirebaseDatabase.getInstance().getReference().child("IsTripOngoingStatus").child(getUid()).setValue(true);

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    void showCall(){
        LatLng passengerLatLng = new LatLng(Double.valueOf(currOrder.sourceLat), Double.valueOf(currOrder.sourceLong));
        LatLng destinationLatLng = new LatLng(Double.valueOf(currOrder.destLat), Double.valueOf(currOrder.destLong));

        passengerLoactionTV.setText(currOrder.source);
        destinationTV.setText(currOrder.destination);
        Animations.playYoYoAnimOnMultipleViews(Techniques.FadeOut,1000,driverCard,onlineStatusLayout);
        Animations.makeVisible(
                callBackgroundBV,callCenterAnimView,passengerCallCountdownTV,
                passengerCallCountdownPB,passengerLoactionTV,
                acceptRejectLayout,destinationTV
        );

        Animations.playYoYoAnimOnMultipleViews(
                Techniques.FadeIn,1000,
                callBackgroundBV,callCenterAnimView,passengerCallCountdownTV,
                passengerCallCountdownPB,passengerLoactionTV,
                acceptRejectLayout
        );

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(passengerLatLng,(float) 15.5));
        passengerMarker = mMap.addMarker(
                new MarkerOptions().
                        position(passengerLatLng).
                        icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
        );

        if(mLastLocation != null){
            LatLng currLatLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            googleDirectionsApiWrapper.
                    animateMapToShowFullPath(false).
                    from(currLatLng).
                    to(passengerLatLng).
                    retreiveDirections().
                    setMap(mMap).
                    drawPathOnMap();
        }

    }

    private void setAllOnClickListeners(){
        setOnClickListenersOnAcceptCallButton();
        setOnClickListenerOnRejectCallButton();
        setOnClickListenerOnCabArrivedButton();
        setOnClickListenerOnPassengerBoardButton();
        setOnClickListenerOnEndTripButton();
        setOnClickListenerOnOpenDrawerButton();
        setOnClickListenerOnStartTripButton();
    }

    private void setOnClickListenersOnAcceptCallButton(){
        acceptCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animations.makeVisible(cabArrivedButton,passengerCard);

                Animations.playYoYoAnimOnMultipleViews(
                        Techniques.FadeOut,1000,callBackgroundBV,passengerCallCountdownTV,
                        passengerCallCountdownPB,passengerLoactionTV,callCenterAnimView,
                        acceptRejectLayout,destinationTV
                );

                Animations.playYoYoAnimOnMultipleViews(Techniques.FadeIn,1000,cabArrivedButton,passengerCard);

                if(mLastLocation != null){
                    googleDirectionsApiWrapper.removePath();
                    LatLng orderLatLng = new LatLng(Double.valueOf(currOrder.sourceLat),Double.valueOf(currOrder.sourceLong));
                    LatLng currLatLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    googleDirectionsApiWrapper.
                            animateMapToShowFullPath(true).
                            from(currLatLng).
                            to(orderLatLng).
                            retreiveDirections().
                            setMap(mMap).
                            drawPathOnMap();
                }

                FirebaseDatabase.getInstance().getReference().
                        child("AcceptedOrders").child(orderCustomerId).
                        setValue(getUid());

                SendNotif sendNotif = new SendNotif(
                        getApplicationContext(),
                        SendNotif.DRIVER_TO_PASSENGER,
                        SendNotif.CAB_FOUND_NOTIF,
                        orderCustomerId
                ).setTitle("Cab Found").send();
                ifArriving=true;


            }
        });
    }

    void setOnClickListenerOnCabArrivedButton(){
        cabArrivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ifArriving=false;
                YoYo.with(Techniques.FadeOut).duration(1000).playOn(cabArrivedButton);
                Animations.makeVisible(passengerBoardedButton);
                YoYo.with(Techniques.FadeIn).duration(1000).playOn(passengerBoardedButton);

                LatLng destinationLatLng = getDestinationLatLng();
                destinationMarker = mMap.addMarker(
                        new MarkerOptions().
                                position(destinationLatLng).
                                icon(BitmapDescriptorFactory.fromResource(R.mipmap.flag))
                );

                googleDirectionsApiWrapper.removePath();


                googleDirectionsApiWrapper.
                        animateMapToShowFullPath(true).
                        from(passengerMarker.getPosition()).
                        to(destinationLatLng).
                        retreiveDirections().
                        setMap(mMap).
                        drawPathOnMap();


                SendNotif sendNotif = new SendNotif(
                        getApplicationContext(),
                        SendNotif.DRIVER_TO_PASSENGER,
                        SendNotif.CAB_ARRIVED_NOTIF,
                        orderCustomerId
                ).setTitle("Cab arrived at pick up").send();
                FirebaseDatabase.getInstance().getReference().child("CabArrived").child(orderCustomerId).setValue(currOrder);
                // Notification to the passenger that the cab has arrived

            }
        });
    }

    private void setOnClickListenerOnPassengerBoardButton(){
        passengerBoardedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inTrip=true;

                Animations.playYoYoAnimOnMultipleViews(Techniques.FadeOut,1000,passengerCard,passengerBoardedButton);
                Animations.makeVisible(endTripButton);
                YoYo.with(Techniques.FadeIn).duration(1000).playOn(endTripButton);
                FirebaseDatabase.getInstance().getReference().child("CabArrived").child(orderCustomerId)
                        .setValue(null);
                FirebaseDatabase.getInstance().getReference().child("StartTrip").child(orderCustomerId)
                        .setValue(currOrder);
                FirebaseDatabase.getInstance().getReference().child("StartTripTime").child(orderCustomerId)
                        .setValue(System.currentTimeMillis()-(60*3*1000));
                SendNotif send = new SendNotif(getApplicationContext(),SendNotif.DRIVER_TO_PASSENGER,SendNotif.CAB_TRIP_STARTED,orderCustomerId);
                send.setTitle("Your Trip Has Started");
                send.send();
            }
        });
    }

    private void setOnClickListenerOnEndTripButton(){
        endTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animations.makeVisible(driverCard, onlineStatusLayout);
                YoYo.with(Techniques.SlideOutDown).duration(500).playOn(endTripButton);

                destinationMarker.remove();
                passengerMarker.remove();
                LatLng currLatLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLatLng,15.5f));
                googleDirectionsApiWrapper.removePath();
                SendNotif send = new SendNotif(getApplicationContext(),SendNotif.DRIVER_TO_PASSENGER,SendNotif.CAB_REACHED_DESTINATION_NOTIF,orderCustomerId);
                send.setTitle("Your Trip Has Ended");
                send.send();
            }
        });
    }

    private void setOnClickListenerOnRejectCallButton(){
        rejectCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removeCallScreen();

                FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                        child(getUid()).
                        child("CustomerId").setValue(orderCustomerId);
                FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                        child(getUid()).
                        child("cabType").setValue(currOrder.cabType);
                FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                        child(getUid()).
                        child("destLat").setValue(currOrder.destLat);
                FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                        child(getUid()).
                        child("destLong").setValue(currOrder.destLong);
                FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                        child(getUid()).
                        child("destination").setValue(currOrder.destination);
                FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                        child(getUid()).
                        child("source").setValue(currOrder.source);
                FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                        child(getUid()).
                        child("sourceLat").setValue(currOrder.sourceLat);
                FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                        child(getUid()).
                        child("sourceLong").setValue(currOrder.sourceLong);


            }
        });
    }

    String getUid(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("UID-SharedPref",0);
        String uid = sharedPref.getString("UID",null);
        Log.v("User Id: ", uid);
        return uid;
    }

    void removeCallScreen(){
        Animations.makeVisible(onlineStatusLayout,driverCard);
        Animations.makeInvisible(
                callBackgroundBV,passengerCallCountdownTV,passengerCallCountdownPB,
                passengerLoactionTV,acceptRejectLayout,destinationTV
        );
        Animations.remove(callCenterAnimView);

        if(mLastLocation != null) {
            LatLng currLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, (float) 15.5));
            passengerMarker.remove();
        }

        googleDirectionsApiWrapper.removePath();
    }

    private LatLng getPassengerLatLng(){
        LatLng passengerLatLng = new LatLng(33.642827, 72.990675);
        return passengerLatLng;
    }

    private LatLng getDestinationLatLng(){
        LatLng destinationLatLng = new LatLng(33.892827, 72.330675);
        return destinationLatLng;
    }

    void linkViews() {
        driverCard = (CardView) findViewById(R.id.driver_card);
        onlineStatusLayout = (RelativeLayout) findViewById(R.id.online_status_layout);
        onlineTextView = (TextView) findViewById(R.id.online_status_text_view);
        onlineSwitch = (Switch) findViewById(R.id.online_switch);
        onlineStatusBarDriverCard = (LinearLayout) findViewById(R.id.online_status_bar_driver_card);
        callBackgroundBV = (BlurView) findViewById(R.id.call_background);
        callCenterAnimView = (AVLoadingIndicatorView) findViewById(R.id.call_center_animation);
        passengerCallCountdownTV = (TextView) findViewById(R.id.passenger_call_countdown_text_view);
        passengerCallCountdownPB = (ProgressBar) findViewById(R.id.passenger_call_countdown_progress_bar);
        acceptCallButton = (Button) findViewById(R.id.accept_passenger_button);
        rejectCallButton = (Button) findViewById(R.id.reject_passenger_button);
        acceptRejectLayout = (LinearLayout) findViewById(R.id.accept_reject_linear_layout);
        passengerLoactionTV = (TextView) findViewById(R.id.passenger_location_text_view);
        passengerNameTV = (TextView) findViewById(R.id.passenger_name_text_view);
        passengerIV = (ImageView) findViewById(R.id.passenger_image_view);
        passengerCard = (CardView) findViewById(R.id.passenger_id_card_view);
        cabArrivedButton = (Button) findViewById(R.id.cab_arrived_button);
        passengerBoardedButton = (Button) findViewById(R.id.passenger_boarded_button);
        endTripButton = (Button) findViewById(R.id.end_trip_button);
        destinationTV = (TextView) findViewById(R.id.passenger_destination_text_view);
        mOpenDrawerButton = (ImageButton) findViewById(R.id.open_nav_drawer);
        startTripButton = (Button) findViewById(R.id.start_trip_button);
    }

    void setupGoogleLocationsAPI() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /**
         * He we will get the user's current
         * location and setup the map camera
         * to that location
         */
        connected=true;
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng currentLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder().
                            target(currentLoc).
                            zoom((float) 15.5).
                            tilt((float) 70).
                            build()
            ));

            cabMarker = mMap.addMarker(new
                    MarkerOptions().
                    position(currentLoc).
                    icon(BitmapDescriptorFactory.fromResource(R.mipmap.cab_icon))
            );
        } else {
            Log.e("Location: ", " Location was null");
        }
        if(isOnline){
            Toast.makeText(getApplicationContext(), "The Trip has been cancelled", Toast.LENGTH_LONG);
            onlineSwitch.setChecked(true);
            startLocationUpdates();
            onlineStatusLayout.setBackgroundColor(Color.parseColor("#00C77C"));
            onlineTextView.setText("Online");
            onlineTextView.setTextColor(Color.parseColor("#ffffff"));
            onlineStatusBarDriverCard.setBackgroundColor(Color.parseColor("#00C77C"));

        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.sign_out) {
            // Handle the camera action
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("GoogleApiClient: ", "Connection Suspended. Trying to reconnect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("GoogleApiClient: ", "Connection Failed. Trying to reconnect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mGoogleApiClient.connect();
        Log.v("GoogleApiClient: ", "Connecting google api client");
    }

    void setupFirebaseAuthentication() {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    ((tApplication)getApplication()).putUid(user.getUid());

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    startActivity(intent);
                }
                // ...
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        FirebaseDatabase.getInstance().getReference().
                child("DriverLocation").
                child(getUid()).
                removeValue();
        mGoogleApiClient.disconnect();
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        View v = findViewById(R.id.map);
        View v1 = findViewById(R.id.driver_card);
        if (v != null && v1 != null) {
            int paddingTop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? getStatusBarHeight() : 0;
            RelativeLayout.LayoutParams mapLayoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
            RelativeLayout.LayoutParams driverCardLayoutParams = (RelativeLayout.LayoutParams) v1.getLayoutParams();
            mapLayoutParams.topMargin -= paddingTop;
            driverCardLayoutParams.topMargin += paddingTop;
            driverCard.setLayoutParams(driverCardLayoutParams);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setStatusBarAlpha(0.2f);
        tintManager.setNavigationBarAlpha(0.2f);
        tintManager.setTintAlpha(0.2f);
        tintManager.setStatusBarTintResource(R.drawable.selected);
        tintManager.setTintColor(Color.parseColor("#007DC0"));
    }

    void attachCallListener(){
        FirebaseDatabase.getInstance().getReference().child("TaxiCustomerMatched").child(getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    orderRecieved = true;
                    Toast.makeText(getApplicationContext(),"Call agaye hai!"+dataSnapshot.getValue(),Toast.LENGTH_SHORT).show();
                    final String cutomerId= (String) dataSnapshot.getValue();
                    orderCustomerId = cutomerId;


                    FirebaseDatabase.getInstance().getReference().child("Order").child(cutomerId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                currOrder = dataSnapshot.getValue(Order.class);
                                showCall();

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    FirebaseDatabase.getInstance().getReference().child("CanceledTrips").child(orderCustomerId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String driverId = (String) dataSnapshot.getValue();
                                if (driverId.equals(getUid())) {
                                    FirebaseDatabase.getInstance().getReference().
                                            child("CanceledTrips").
                                            child(orderCustomerId).
                                            removeValue();

                                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean("AfterTripCancelled", true);
                                    editor.commit();

                                    tripCancelled = true;

                                    Intent intent = new Intent(getApplicationContext(), DriverMapActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    finish();
                                    startActivity(intent);

                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected int getPixelsFromDPs(float dps) {
        Resources r = getResources();
        int px = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dps, r.getDisplayMetrics()));
        return px;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(orderRecieved && !tripCancelled) {
            FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                    child(getUid()).
                    child("CustomerId").setValue(orderCustomerId);
            FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                    child(getUid()).
                    child("cabType").setValue(currOrder.cabType);
            FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                    child(getUid()).
                    child("destLat").setValue(currOrder.destLat);
            FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                    child(getUid()).
                    child("destLong").setValue(currOrder.destLong);
            FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                    child(getUid()).
                    child("destination").setValue(currOrder.destination);
            FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                    child(getUid()).
                    child("source").setValue(currOrder.source);
            FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                    child(getUid()).
                    child("sourceLat").setValue(currOrder.sourceLat);
            FirebaseDatabase.getInstance().getReference().child("RejectedOrders").
                    child(getUid()).
                    child("sourceLong").setValue(currOrder.sourceLong);
        }
    }

    void getLocationPermissions() {
        Log.v("LocationPermissions: ", "Getting Location Permissions");
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
                        Constants.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

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
            case Constants.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
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

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
            LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());


            Log.v("CabLocation: ", "Moving cab marker to new location");
            cabMarker.setPosition(currentLoc);
            mLastLocation = location;

            FirebaseDatabase.getInstance().getReference().child("DriverLocation").child(getUid()).
                    setValue(new DriverLocation(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude())));

            if(inTrip){
                FirebaseDatabase.getInstance().getReference().child("TripPath").child(orderCustomerId+getUid()).push().setValue(
                        new DriverLocation(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()))
                );
                mTripPathData.add(new LatLng(location.getLatitude(),location.getLongitude()));
            }
            if(ifArriving){
                googleDirectionsApiWrapper.removePath();
                googleDirectionsApiWrapper.animateMapToShowFullPath(false)
                        .from(currentLoc)
                        .retreiveDirections()
                        .setMap(mMap)
                        .drawPathOnMap();
            }

        }


    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


}
