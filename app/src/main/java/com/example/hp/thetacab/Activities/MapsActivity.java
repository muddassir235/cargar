package com.example.hp.thetacab.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.thetacab.Animations;
import com.example.hp.thetacab.Constants;
import com.example.hp.thetacab.DriverLocation;
import com.example.hp.thetacab.GoogleDirectionsApiWrapper;
import com.example.hp.thetacab.GoogleReverseGeocodingApiWrapper;
import com.example.hp.thetacab.Order;
import com.example.hp.thetacab.R;
import com.example.hp.thetacab.SystemBarTintManager;
import com.example.hp.thetacab.Utils;
import com.example.hp.thetacab.tApplication;
import com.github.hujiaweibujidao.wava.Techniques;
import com.github.hujiaweibujidao.wava.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eightbitlab.com.blurview.BlurView;

public class MapsActivity
        extends FragmentActivity
        implements OnMapReadyCallback,
            GoogleApiClient.OnConnectionFailedListener,
            GoogleApiClient.ConnectionCallbacks,
            NavigationView.OnNavigationItemSelectedListener
{

    private static final String TAG = "MapActivity";

    //APIs
    FirebaseAuth mAuth;
    private GoogleMap mMap;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    android.location.Location mLastLocation;
    GoogleDirectionsApiWrapper googleDirectionsApiWrapper;

    //Views
    @InjectView(R.id.notify_selection_toast_text_view) TextView notifySelectionToastTV;
    @InjectView(R.id.bike_selection_image_button) ImageButton bikeSelectionIB;
    @InjectView(R.id.sedan_selecton_image_button) ImageButton sedanSelectionIB;
    @InjectView(R.id.suv_selection_image_button) ImageButton suvSelectionIB;
    @InjectView(R.id.cab_selection_card_view) CardView cabSelectionCV;
    @InjectView(R.id.cab_type_selection_layout) LinearLayout cabSelectionLayout;
    @InjectView(R.id.button_on_top_of_marker) CardView markerButton;
    @InjectView(R.id.search_source_card_view) CardView searchSouceCardView;
    @InjectView(R.id.search_destination_card_view) CardView searchDestinationCardView;
    @InjectView(R.id.source_destination_selection_layout) CardView sourceDestinationSelectionLayoutCV;
    @InjectView(R.id.marker_button_text_view) TextView markerButtonTextView;
    @InjectView(R.id.marker_at_center_of_map_image_view) ImageView markerAtCenterOfMapIV;
    @InjectView(R.id.source_bar_cross_image_button) ImageButton sourceBarCrossIB;
    @InjectView(R.id.destination_bar_cross_image_button) ImageButton destinationBarCrossIB;
    @InjectView(R.id.request_cab_button) Button requestCabButton;
    @InjectView(R.id.request_cab_price_text_view) TextView fairQuoteTV;
    @InjectView(R.id.blurView) BlurView blurView;
    @InjectView(R.id.finding_taxi_text_view) TextView findingTaxiTV;
    @InjectView(R.id.finding_taxi_animation_view) AVLoadingIndicatorView findCabAnimView;
    @InjectView(R.id.eta_of_cab_text_view) TextView etaOfCabTV;
    @InjectView(R.id.driver_card_display_frame_layout) FrameLayout driverCardHolderFL;
    @InjectView(R.id.driver_card) CardView driverCard;
    @InjectView(R.id.cancel_trip_button) Button cancelTripButton;
    @InjectView(R.id.cab_has_arrived_text_view) TextView cabHasArrivedTV;
    @InjectView(R.id.open_nav_drawer) ImageButton mOpenDrawerButton;
    @InjectView(R.id.cab_arrived_animation) AVLoadingIndicatorView cabHasArrivedAnimView;
    PlaceAutocompleteFragment sourceAddressAutocCompleteFragment;
    PlaceAutocompleteFragment destinationAddressAutoCompleteFragment;

    //Fields
    int noOfAnimationsRunning;
    boolean movingUpAnimation;
    boolean movingDownAnimation;
    boolean sourceEntered;
    boolean destinationEntered;
    boolean canTapSelectionIcon;
    boolean driverIsArriving;
    boolean inTrip;
    private int currentCabSelection;
    private int currentAppState;
    private String driverId;
    private LatLng cabLatLng;
    private String instanceId;
    private boolean cabFoundScreenHadBeenShown;
    private LatLng sourceLatLng;
    private LatLng destinationLatLng;
    private Marker sourceMarker;
    private Marker destinationMarker;
    private Marker cabMarker;
    public LatLng centerOfMapLatLng;
    private String DIRECTION_API_KEY;
    private String sourceAddress;
    private String destinationAddress;
    private ArrayList<LatLng> mTripPathData;

    //////////////////<Actvity Methods>///////////////////////
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.inject(this);

        setupAllViews();
        setupFirebaseAuthentication();
        initializeFields();
        setupGooglePlacesAPI();

        setStatusBarTranslucent(true);
        getLocationPermissions();
        FirebaseMessaging.getInstance().subscribeToTopic("notif");
        googleDirectionsApiWrapper.setEtaTV(etaOfCabTV);
        googleDirectionsApiWrapper.setFairEstimateTV(fairQuoteTV);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    //////////////////</Actvity Methods>//////////////////////////////////

    //////////////////////<Setup>////////////////////////////////////
    //////////<Start Setup Functions>//////////////////////////////

    /**
     * Initialize all the fiels of the Activity
     */
    void initializeFields(){
        DIRECTION_API_KEY = getResources().getString(R.string.google_directions_api_key);
        googleDirectionsApiWrapper = new GoogleDirectionsApiWrapper(DIRECTION_API_KEY);
        googleDirectionsApiWrapper.setCabType(1);
        googleDirectionsApiWrapper.changeEstimateWhenCabTypeChanged();
        canTapSelectionIcon = true;
        currentCabSelection = Constants.SELECT_SEDAN;
        noOfAnimationsRunning = 0;
        movingUpAnimation = false;
        movingDownAnimation = false;
        sourceEntered = false;
        destinationEntered = false;
        cabFoundScreenHadBeenShown = false;
        driverIsArriving = false;
        inTrip = false;
    }

    void launchAppropriateAppState(){
        Log.v("MapsActivity: ","Launch Appropriate App State");
        if(currentAppState == 1){
            // do nothing
            Toast.makeText(getApplicationContext(),"Should Launch into 1st App State",Toast.LENGTH_SHORT).show();
        }else if(currentAppState ==2){
            Toast.makeText(getApplicationContext(),"Should Launch into 2nd App State",Toast.LENGTH_SHORT).show();
            Animations.makeVisible(findCabAnimView,findingTaxiTV,blurView);
            Animations.makeInvisible(
                    requestCabButton,cabSelectionCV,notifySelectionToastTV,
                    sourceDestinationSelectionLayoutCV,etaOfCabTV,driverCard,
                    cancelTripButton,driverCardHolderFL,cabHasArrivedAnimView,
                    cabHasArrivedTV,markerAtCenterOfMapIV,markerButton
            );
        }else if(currentAppState == 3){
            Animations.makeInvisible(
                    findingTaxiTV,requestCabButton,cabSelectionCV,
                    notifySelectionToastTV,sourceDestinationSelectionLayoutCV,
                    blurView,cabHasArrivedAnimView,cabHasArrivedTV,
                    markerAtCenterOfMapIV,markerButton
            );
            Animations.makeVisible(
                    etaOfCabTV,driverCard,
                    driverCardHolderFL,cancelTripButton
            );
            Animations.remove(findCabAnimView);

            Toast.makeText(getApplicationContext(),"Should Launch into 3rd App State",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(),"Should Launch into 4th App State",Toast.LENGTH_SHORT).show();
            Animations.makeInvisible(
                    findingTaxiTV,cabHasArrivedAnimView,cabSelectionCV,
                    notifySelectionToastTV,sourceDestinationSelectionLayoutCV,etaOfCabTV,
                    driverCard,requestCabButton,cancelTripButton,
                    driverCardHolderFL,cabHasArrivedAnimView,markerButton,markerAtCenterOfMapIV
            );
            Animations.makeVisible(blurView,cabHasArrivedAnimView,cabHasArrivedTV);
            Animations.remove(findCabAnimView);
            cabHasArrivedAnimView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cabHasArrivedAnimView.setVisibility(View.INVISIBLE);
                }
            },2000);

        }
    }

    void setupGooglePlacesAPI() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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
                    FirebaseDatabase.getInstance().getReference().
                            child("State").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                currentAppState = Integer.valueOf(String.valueOf((dataSnapshot.getValue())));
                                launchAppropriateAppState();
                            }
                            else{
                                currentAppState=1;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            currentAppState = 1;
                            launchAppropriateAppState();
                        }
                    });

                    FirebaseDatabase.getInstance().getReference().child("Customers").
                            child(getUid()).child("Type").
                            addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String customerType = (String) dataSnapshot.getValue();
                                    if(customerType.equals("Customer")){

                                    }else if(customerType.equals("Driver")){
                                        Intent intent = new Intent(getApplicationContext(),DriverMapActivity.class);
                                        finish();
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

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

    void setupAllViews() {
        linkViews();

        setAllOnClickListeners();

        setOnPlaceSelectedListenerOnSourceBar();
        setOnPlaceSelectedListnerOnDestinationBar();

        //Set the hints in the source and destination bars.
        sourceAddressAutocCompleteFragment.setHint("Enter Source");
        destinationAddressAutoCompleteFragment.setHint("Enter Destination");

        //Set Max Card Elevation for source and destination Address Cards
        searchDestinationCardView.setMaxCardElevation(getPixelsFromDPs(20));
        searchSouceCardView.setMaxCardElevation(getPixelsFromDPs(20));
        cabSelectionCV.setMaxCardElevation(getPixelsFromDPs(30));
        cabSelectionCV.setCardElevation(getPixelsFromDPs(10));
    }

    ///////////<Setup Views Functions>///////////////////////////////////

    /**
     * Link Java Fields to XML
     */
    void linkViews() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            sourceAddressAutocCompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_source);
            destinationAddressAutoCompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destination);
        }
    }

    //////////////<Setup Listener Functions>/////////////////////////////////
    void setAllOnClickListeners() {
        setOnClickListenersOnCabTypeSelection();
        setOnClickListnerForMarkerButton();
        setOnCancelListenerForSourceCard();
        setOnCancelListenerForDestinationCard();
        setOnClickListenerOnRequestCabButton();
        setOnClickListenerOnCancelTripButton();
        setOnClickListenerOnOpenDrawerButton();
    }

    ////////////////<Set OnClick Listeners>/////////////////////////////////////

    void setOnClickListenerOnRequestCabButton(){
        requestCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("Order").child(getUid()).setValue(new Order(
                        sourceAddress,
                        destinationAddress,
                        String.valueOf(sourceLatLng.latitude),
                        String.valueOf(sourceLatLng.longitude),
                        String.valueOf(destinationLatLng.latitude),
                        String.valueOf(destinationLatLng.longitude),
                        currentCabSelection
                ));

                mDatabase.child("State").child(getUid()).setValue(Constants.FINDING_CAB_STATE);
                // move the souce and destination selection card view up
                animateSourceDestinatonSelectionLayoutUp();

                // move the request button with it's text view and the cab selection card view down.
                animateCarTypeSelectionAndRequestLayoutDown();

                //show finding cab animation
                showFindingCabScreen();

                FirebaseDatabase.getInstance().getReference().
                        child("AcceptedOrders").
                        child(getUid()).
                        addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    driverIsArriving = true;
                                    driverId = (String) dataSnapshot.getValue();


                                    FirebaseDatabase.getInstance().getReference().child("AcceptedOrders").child(getUid()).removeValue();

                                    FirebaseDatabase.getInstance().getReference().child("DriverLocation").child(driverId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(driverIsArriving) {
                                                if (dataSnapshot.exists()) {
                                                    Log.v("DriverLocation:::: ", "datasnapshot exists.");

                                                    if (!cabFoundScreenHadBeenShown) {
                                                        removeFindingCabScreen();
                                                        cabFoundScreenHadBeenShown = true;
                                                    }
                                                    HashMap cabLatLngHashMap = (HashMap) dataSnapshot.getValue();
                                                    double cabLat = Double.valueOf((String) cabLatLngHashMap.get("Lat"));
                                                    double cabLong = Double.valueOf((String) cabLatLngHashMap.get("Long"));
                                                    cabLatLng = new LatLng(cabLat, cabLong);
                                                    animateToCabsPositon(cabLatLng);
                                                    showPathBetweenCabAndPassenger(sourceLatLng, cabLatLng);

                                                } else {
                                                    Log.v("DriverLocation:::: ", "datasnapshot doesn't exist.");
                                                }
                                            } else if( inTrip ){
                                                mTripPathData = new ArrayList<LatLng>();
                                                FirebaseDatabase.getInstance().getReference().child("TripPath").child(getUid()+driverId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                                int distanceCoveredInMeters = Utils.getDistanceInMetersFromLatLngData(mTripPathData);
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
                                    setOnCabArrivedListener();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }
        });
    }

    String getUid(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("UID-SharedPref",0);
        String uid = sharedPref.getString("UID",null);
        Log.v("User Id: ", uid);
        return uid;
    }

    void setOnCabArrivedListener(){
        FirebaseDatabase.getInstance().getReference().child("CabArrived").child(getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.v("CabArrived", " Inside on cab arrived listener");
                if(dataSnapshot.exists()) {
                    final Order order = dataSnapshot.getValue(Order.class);

                    if(driverId!=null) {
                        driverIsArriving = false;
                        FirebaseDatabase.getInstance().getReference().child("DriverLocation").child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    driverIsArriving = false;
                                    HashMap cabLatLngHashMap = (HashMap) dataSnapshot.getValue();
                                    double cabLat = Double.valueOf((String) cabLatLngHashMap.get("Lat"));
                                    double cabLong = Double.valueOf((String) cabLatLngHashMap.get("Long"));
                                    cabMarker.setPosition(new LatLng(cabLat, cabLong));
                                    googleDirectionsApiWrapper.removePath();
                                    googleDirectionsApiWrapper.animateMapToShowFullPath(true).
                                            from(new LatLng(cabLat, cabLong)).
                                            to(new LatLng(Double.valueOf(order.destLat), Double.valueOf(order.destLong))).
                                            retreiveDirections().
                                            setMap(mMap).
                                            drawPathOnMap();

                                    cabMarker.setTitle("Your cab has arrived");
                                    cabMarker.showInfoWindow();
                                    Animations.remove(etaOfCabTV, cancelTripButton);
                                    Animations.makeVisible(driverCard, driverCardHolderFL);
                                    setOnTripStatedListener();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }else{
                    Log.v("CabArrived", " data snapshot doesn't exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void showFindingCabScreen(){

        Animations.makeVisible(findCabAnimView,findingTaxiTV,blurView);
        // show loading animation
        Animations.playYoYoAnimOnMultipleViews(Techniques.FadeIn,1000,findCabAnimView,findingTaxiTV,blurView);
        LatLng sourceLatLng = sourceMarker.getPosition();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(sourceLatLng));
    }

    void removeFindingCabScreen(){
        YoYo.Builder builder = YoYo.with(Techniques.FadeOut).duration(1000).interpolate(new AccelerateDecelerateInterpolator());
        builder.playOn(findingTaxiTV);

        Handler handlerForDelayingCabFoundAnimation = new Handler();
        handlerForDelayingCabFoundAnimation.postDelayed(new Runnable() {
            @Override
            public void run() {
                findingTaxiTV.setText("CAB FOUND");
                findingTaxiTV.setTextColor(Color.parseColor("#F9BA32"));
                YoYo.with(Techniques.FadeIn).duration(300).interpolate(new AccelerateDecelerateInterpolator()).playOn(findingTaxiTV);

                Handler waitToReachTheCabAndTheShowThePath = new Handler();
                waitToReachTheCabAndTheShowThePath.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animations.makeVisible(etaOfCabTV);
                        YoYo.with(Techniques.SlideInDown).duration(500).playOn(etaOfCabTV);

                        Animations.makeVisible(driverCard,driverCardHolderFL);
                        Animations.playYoYoAnimOnMultipleViews(Techniques.SlideInUp,1000,driverCardHolderFL,driverCard);

                        Animations.makeVisible(cancelTripButton);
                        YoYo.with(Techniques.SlideInUp).duration(500).playOn(cancelTripButton);
                        Animations.remove(findingTaxiTV,findCabAnimView);
                    }
                },1000);

            }
        },350);

        YoYo.Builder hideView = builder.delay(1650);
        hideView.playOn(findingTaxiTV);
        hideView.playOn(findCabAnimView);
        hideView.playOn(blurView);
    }

    void animateToCabsPositon(LatLng currCabLatLng){
        if(cabMarker!=null){
            cabMarker.remove();
        }

        googleDirectionsApiWrapper.removePathThatIsVisible();

        cabMarker = mMap.addMarker(new MarkerOptions().
                title("Cab").
                position(currCabLatLng).
                icon(BitmapDescriptorFactory.fromResource(R.mipmap.cab_icon))
        );
    }

    void showPathBetweenCabAndPassenger(LatLng currentCabLatLng,LatLng passengerLatLng){
        if(!cabFoundScreenHadBeenShown) {
            googleDirectionsApiWrapper.
                    animateMapToShowFullPath(true).
                    from(currentCabLatLng).
                    to(passengerLatLng).
                    retreiveDirections().
                    setMap(mMap).
                    drawPathOnMap();
        }else{
            googleDirectionsApiWrapper.
                    animateMapToShowFullPath(false).
                    from(currentCabLatLng).
                    to(passengerLatLng).
                    retreiveDirections().
                    setMap(mMap).
                    drawPathOnMap();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("UID-SharedPref",0);
        String uId = sharedPref.getString("UID",null);
        if(uId!=null) {
            FirebaseDatabase.getInstance().getReference().child("AppStatus").child(uId).setValue(0);
        }
    }

    void showCabHasArrivedScreen(){
        YoYo.Builder hide = YoYo.with(Techniques.FadeOut).duration(1000).interpolate(new AccelerateDecelerateInterpolator());
        Animations.makeVisible(cabHasArrivedTV,cabHasArrivedAnimView);
        Animations.playYoYoAnimOnMultipleViews(Techniques.FadeIn,1000,cabHasArrivedTV,blurView,cabHasArrivedAnimView);
        Animations.playYoYoAnimOnMultipleViews(Techniques.FadeOut,1000,cancelTripButton,driverCardHolderFL,driverCard,etaOfCabTV);
        hide.delay(1000).playOn(cabHasArrivedAnimView);
    }
    /**
     * Click Listener for:
     *  When the user taps to select a bike
     */
    void setOnClickListenersOnCabTypeSelection(){
        bikeSelectionIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCabSelection = Constants.SELECT_BIKE;
                googleDirectionsApiWrapper.setCabType(currentCabSelection);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bikeSelectionIB.setImageTintList(ColorStateList.valueOf(Color.parseColor("#007DC0")));
                    sedanSelectionIB.setImageTintList(ColorStateList.valueOf(Color.parseColor("#79868E")));
                    suvSelectionIB.setImageTintList(ColorStateList.valueOf(Color.parseColor("#79868E")));
                }
                animateSelectionToast("YOU HAVE SELECTED A BIKE");
            }
        });
        sedanSelectionIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCabSelection = Constants.SELECT_SEDAN;
                googleDirectionsApiWrapper.setCabType(currentCabSelection);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bikeSelectionIB.setImageTintList(ColorStateList.valueOf(Color.parseColor("#79868E")));
                    sedanSelectionIB.setImageTintList(ColorStateList.valueOf(Color.parseColor("#007DC0")));
                    suvSelectionIB.setImageTintList(ColorStateList.valueOf(Color.parseColor("#79868E")));
                }
                animateSelectionToast("YOU HAVE SELECTED A SEDAN");
            }
        });
        suvSelectionIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCabSelection = Constants.SELECT_SUV;
                googleDirectionsApiWrapper.setCabType(currentCabSelection);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bikeSelectionIB.setImageTintList(ColorStateList.valueOf(Color.parseColor("#79868E")));
                    sedanSelectionIB.setImageTintList(ColorStateList.valueOf(Color.parseColor("#79868E")));
                    suvSelectionIB.setImageTintList(ColorStateList.valueOf(Color.parseColor("#007DC0")));
                }
                animateSelectionToast("YOU HAVE SELECTED AN SUV");
            }
        });
    }

    void setOnClickListenerOnOpenDrawerButton(){
        mOpenDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(Gravity.START);
            }
        });
    }

    void setOnTripStatedListener(){
        FirebaseDatabase.getInstance().getReference().child("StartTrip").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    inTrip = true;
                    Order order = dataSnapshot.getValue(Order.class);
                    Animations.playYoYoAnimOnMultipleViews(Techniques.SlideOutDown,1000,driverCard,driverCardHolderFL);
                    Animations.makeVisible(etaOfCabTV);
                    YoYo.with(Techniques.SlideInDown).duration(1000).playOn(etaOfCabTV);
                    etaOfCabTV.setText("Have a safe journey.");
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            etaOfCabTV.setText("2km has been covered in 5 minutes, the current fair estimate is 100 PKR");
                        }
                    },3000);

                }else{
                    Log.e("StartTrip", " datasnapshot doesn't exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    /**
     * Click Listener for:
     *  When the user taps the button at the
     *  center of the screen for setting a
     *  place as a source or destination
     */
    void setOnClickListnerForMarkerButton() {
        markerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerOfMapLatLng = mMap.getCameraPosition().target;
                GoogleReverseGeocodingApiWrapper googleReverseGeocodingApiWrapper = new GoogleReverseGeocodingApiWrapper(DIRECTION_API_KEY);
                googleReverseGeocodingApiWrapper.setLatLng(centerOfMapLatLng).
                        requestAddress().setOnAddressRetrievedListener(new GoogleReverseGeocodingApiWrapper.OnAddressRetrievedListener() {
                    @Override
                    public void onAddressRetrieved(String resultingAddress) {
                        if(!sourceEntered && !destinationEntered) {
                            /**
                             * make the cross button on the source card visible
                             * so that the user can cancel the source that they
                             * have set if they want to enter a different one.
                             */
                            sourceBarCrossIB.setVisibility(View.VISIBLE);

                            /**
                             * put the current address in the source bar as the
                             * user has selected this place as thier source
                             */
                            sourceAddressAutocCompleteFragment.setText(resultingAddress);
                            sourceAddress = resultingAddress;

                            /**
                             * Adjust the view for entering destination mode
                             */
                            turnMarkerIntoSetDestinationMarker();
                            sourceEnteredHighLightDestinationBar();

                            // Add a marker a the source indicating that it is the source
                            sourceMarker = mMap.addMarker(new MarkerOptions()
                                    .position(centerOfMapLatLng)
                                    .title("Source").icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_green)));
                            sourceMarker.setVisible(true);

                            // the source has been entered
                            sourceEntered = true;

                            // this is the source LatLng
                            sourceLatLng = centerOfMapLatLng;
                        }else if(sourceEntered && ! destinationEntered){
                            /**
                             * make the cross button on the destination bar
                             * visible so that the user cancel the destination
                             * if they want to enter a different one.
                             */
                            destinationBarCrossIB.setVisibility(View.VISIBLE);

                            /**
                             * Put the current place's address as the destination in
                             * the destination bar as the user has selected this
                             * place as thier destination
                             */
                            destinationAddressAutoCompleteFragment.setText(resultingAddress);
                            destinationAddress = resultingAddress;

                            /**
                             * make marker and button disappear as both source and destination have
                             * been entered.
                             */
                            makeMarkerDisappear();

                            makeDestinationBarBlue();

                            // this is the destination LatLng
                            destinationLatLng = centerOfMapLatLng;

                            // draw path between source and destination
                            googleDirectionsApiWrapper.from(sourceLatLng).to(destinationLatLng).retreiveDirections().setTextView(fairQuoteTV).setMap(mMap).drawPathOnMap();

                            /**
                             * Add a marker at this place clearly indication that it is
                             * the destination.
                             */
                            destinationMarker = mMap.addMarker(new MarkerOptions()
                                    .position(centerOfMapLatLng)
                                    .title("Destination").icon(BitmapDescriptorFactory.fromResource(R.mipmap.flag_blue)));
                            destinationMarker.setVisible(true);

                            // the destination has been entered
                            destinationEntered = true;

                            /**
                             * as both source and destination have been entered
                             * make the request cab UI visible to the user.
                             */
                            animateRequestCabView(true);

                        }else if(!sourceEntered && destinationEntered){
                            /**
                             * make the cross button on the source card visible
                             * so that the user can cancel the source that they
                             * have set if they want to enter a different one.
                             */
                            sourceBarCrossIB.setVisibility(View.VISIBLE);

                            /**
                             * put the current address in the source bar as the
                             * user has selected this place as thier source
                             */
                            sourceAddressAutocCompleteFragment.setText(resultingAddress);
                            sourceAddress = resultingAddress;

                            /**
                             * make marker and button disappear as both source and destination have
                             * been entered.
                             */
                            makeMarkerDisappear();

                            makeSourceBarGreen();

                            // this is the source LatLng
                            sourceLatLng = centerOfMapLatLng;

                            // draw path between source and destination
                            googleDirectionsApiWrapper.from(sourceLatLng).to(destinationLatLng).retreiveDirections().setTextView(fairQuoteTV).setMap(mMap).drawPathOnMap();

                            // Add a marker a the source indicating that it is the source
                            sourceMarker = mMap.addMarker(new MarkerOptions()
                                    .position(centerOfMapLatLng)
                                    .title("Source").icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_green)));
                            sourceMarker.setVisible(true);

                            /**
                             * as both source and destination have been entered
                             * make the request cab UI visible to the user.
                             */
                            animateRequestCabView(true);

                            // the source has been entered
                            sourceEntered = true;


                        }else {
                            /**
                             * This condition is impossible as when
                             * both the source and destination have
                             * been entered the marker button cannot
                             * be clicked
                             */

                        }


                    }
                });

            }
        });
    }

    /**
     * Click Listener for:
     *  When A User Cancels the entry in the source selection bar.
     */
    void setOnCancelListenerForSourceCard(){
        sourceBarCrossIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceBarCrossIB.setVisibility(View.GONE);
                sourceMarker.setVisible(false);
                sourceAddressAutocCompleteFragment.setText("");
                sourceAddressAutocCompleteFragment.setHint("Enter Source");
                sourceEntered = false;
                if(!destinationEntered){
                    elevateSourceBar();
                    makeDestinationBarBlank();
                    turnMarkerIntoSetSourceMarker();
                }else{
                    googleDirectionsApiWrapper.removePath();
                    destinationEnteredHighlightSourceBar();
                    turnMarkerIntoSetSourceMarker();
                    animateRequestCabView(false);
                }
            }
        });
    }

    /**
     * Click Listener for:
     *  When A User Cancels the entry in the destination selection bar.
     */
    void setOnCancelListenerForDestinationCard(){
        destinationBarCrossIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                destinationMarker.setVisible(false);
                destinationBarCrossIB.setVisibility(View.GONE);
                destinationAddressAutoCompleteFragment.setText("");
                destinationAddressAutoCompleteFragment.setHint("Enter Destination");
                destinationEntered = false;
                if(!sourceEntered){
                    elevateSourceBar();
                    makeDestinationBarBlank();
                    turnMarkerIntoSetSourceMarker();
                }else{
                    googleDirectionsApiWrapper.removePath();
                    sourceEnteredHighLightDestinationBar();
                    turnMarkerIntoSetDestinationMarker();
                    animateRequestCabView(false);
                }
            }
        });
    }
    ////////////////</Set OnClick Listners>////////////////////////////////////

    ///////////////<Set On Place Selected Listeners>/////////////////////////

    /**
     * Event Listener for:
     *  When the user types and selects a place in the source selection bar
     */
    void setOnPlaceSelectedListenerOnSourceBar(){
        sourceAddressAutocCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                sourceBarCrossIB.setVisibility(View.VISIBLE);
                sourceEntered = true;
                if(!destinationEntered){
                    // this is the source location
                    sourceLatLng = place.getLatLng();

                    // make the source bar show the complete address of the place
                    sourceAddressAutocCompleteFragment.setText(place.getAddress());
                    sourceAddress = place.getAddress().toString();

                    // animate the camera to the source location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), (float) 15.5));

                    // add a marker to the current location indicating that it is the source
                    sourceMarker = mMap.addMarker(new MarkerOptions().
                            position(place.getLatLng()).
                            icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_green)).
                            title("Source").
                            visible(true)
                    );

                    sourceEnteredHighLightDestinationBar();

                    /**
                     * as the source have been entered turn the marker
                     * and button to set destination mode
                     */
                    turnMarkerIntoSetDestinationMarker();
                }else{
                    // this is the source location
                    sourceLatLng = place.getLatLng();

                    // make the source bar show the complete address of the place
                    sourceAddressAutocCompleteFragment.setText(place.getAddress());
                    sourceAddress = place.getAddress().toString();

                    // animate the camera to the source location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), (float) 15.5));

                    // add a marker to the current location indicating that it is the source
                    sourceMarker = mMap.addMarker(new MarkerOptions().
                            position(place.getLatLng()).
                            icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_green)).
                            title("Source").
                            visible(true)
                    );

                    // draw path between source and destination as both of them have been entered
                    googleDirectionsApiWrapper.from(sourceLatLng).to(destinationLatLng).retreiveDirections().setTextView(fairQuoteTV).setMap(mMap).drawPathOnMap();

                    // make sourcebar green and make marker disappear
                    makeSourceBarGreen();
                    makeMarkerDisappear();

                    // show the request cab UI
                    animateRequestCabView(true);
                }
            }

            @Override
            public void onError(Status status) {

            }
        });
    }

    /**
     * Event Listener for:
     *  When the user types and selects a place in the destination selection bar
     */
    void setOnPlaceSelectedListnerOnDestinationBar(){
        destinationAddressAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destinationBarCrossIB.setVisibility(View.VISIBLE);
                destinationEntered = true;
                if(!sourceEntered){
                    // this is the destination location
                    destinationLatLng = place.getLatLng();

                    // make the destination bar show the complete address of the place
                    destinationAddressAutoCompleteFragment.setText(place.getAddress());
                    destinationAddress = place.getAddress().toString();

                    // animate the camera to the destination location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), (float) 15.5));

                    // add a marker to the current location indicating that it is the destination
                    destinationMarker = mMap.addMarker(new MarkerOptions().
                            position(place.getLatLng()).
                            icon(BitmapDescriptorFactory.fromResource(R.mipmap.flag_blue)).
                            title("Destination").
                            visible(true)
                    );

                    destinationEnteredHighlightSourceBar();

                    /**
                     * as the destination has been entered turn the marker
                     * and button to set source mode
                     */
                    turnMarkerIntoSetSourceMarker();
                }else{
                    // this is the destination location
                    destinationLatLng = place.getLatLng();

                    // make the destination bar show the complete address of the place
                    destinationAddressAutoCompleteFragment.setText(place.getAddress());
                    destinationAddress = place.getAddress().toString();

                    // animate the camera to the destination location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), (float) 15.5));

                    // add a marker to the current location indicating that it is the destination
                    destinationMarker = mMap.addMarker(new MarkerOptions().
                            position(place.getLatLng()).
                            icon(BitmapDescriptorFactory.fromResource(R.mipmap.flag_blue)).
                            title("Destination").
                            visible(true)
                    );

                    // draw path between source and destination as both of them have been entered
                    googleDirectionsApiWrapper.from(sourceLatLng).to(destinationLatLng).retreiveDirections().setTextView(fairQuoteTV).setMap(mMap).drawPathOnMap();


                    makeDestinationBarBlue();
                    makeMarkerDisappear();

                    // show request cab UI
                    animateRequestCabView(true);
                }
            }

            @Override
            public void onError(Status status) {

            }
        });
    }
    //////////////</Set On Place Selected Listeners>//////////////////////////
    /////////////</Setup Listeners Functions>/////////////////////////////////
    ///////////</Setup Views Functions>///////////////////////////////////////
    //////////</Start Setup Functions>/////////////////////////////////////////
    /////////</Setup>/////////////////////////////////////////////////////////


    ///////////////<Event Listeners>///////////////////////////////////////////
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
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
                    // locations-related task you need to do.

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

    void setOnClickListenerOnCancelTripButton(){
        cancelTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().
                        child("CanceledTrips").
                        child(getUid()).
                        setValue(driverId);
                FirebaseDatabase.getInstance().getReference().
                        child("State").child(getUid()).setValue(Constants.SET_SOURCE_STATE);
                //1)find driver state 2)if app open don't do anything else send notification
                if(driverId!=null) {
                    FirebaseDatabase.getInstance().getReference().child("AppStatus").child(driverId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        int status = Integer.valueOf(String.valueOf((long) dataSnapshot.getValue()));
                                        if (status == 0) {
                                            FirebaseDatabase.getInstance().getReference().child("MapUIDtoInstanceID").child(driverId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                instanceId = dataSnapshot.getValue().toString();
                                                                SendNotif send = new SendNotif();
                                                                send.execute(Constants.CANCEL_TRIP);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }



                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                startActivity(intent);
            }
        });
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /**
         * He we will get the user's current
         * location and setup the map camera
         * to that location
         */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        }else{
            Log.e("Location: ", " Location was null");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("GoogleApiClient: ", "Connection Suspended. Trying to reconnect");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("UID-SharedPref",0);
        String uId = sharedPref.getString("UID",null);
        if(uId != null) {
            FirebaseDatabase.getInstance().getReference().child("AppStatus").child(uId).setValue(1);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("GoogleApiClient: ", "Connection Failed. Trying to reconnect");
        mGoogleApiClient.connect();
    }

    //////////////</Event Listeners>/////////////////////////////////////////////////

    /////////////////<Manipulate Views>//////////////////////////////////////////////

    /**
     * Once the source has been set
     * if the destination hasn't yet
     * been entered make the source
     * bar green and highlight the
     * destination bar
     */
    void sourceEnteredHighLightDestinationBar(){
        makeSourceBarGreen();
        elevateDestinationBar();
    }

    /**
     * Once the destination has been
     * set if the source has not been
     * entered make the destination bar
     * blue and highlight the source bar
     */
    void destinationEnteredHighlightSourceBar(){
        elevateSourceBar();
        makeDestinationBarBlue();
    }

    /**
     * Adjust the marker and button
     * at the center of the screen
     * for setting source.
     */
    void turnMarkerIntoSetSourceMarker(){
        markerButton.setVisibility(View.VISIBLE);
        markerAtCenterOfMapIV.setVisibility(View.VISIBLE);
        markerButtonTextView.setText("SET SOURCE");
        markerButton.setCardBackgroundColor(Color.parseColor("#ddF9BA32"));
        markerAtCenterOfMapIV.setImageResource(R.mipmap.marker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            markerAtCenterOfMapIV.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F9BA32")));
        }
    }

    /**
     * Adjust the marker and button
     * at the center of the screen
     * for setting destiantion.
     */
    void turnMarkerIntoSetDestinationMarker(){
        markerButton.setVisibility(View.VISIBLE);
        markerAtCenterOfMapIV.setVisibility(View.VISIBLE);
        markerButtonTextView.setText("SET DESTINATION");
        markerButton.setCardBackgroundColor(Color.parseColor("#dd426E86"));
        markerAtCenterOfMapIV.setImageResource(R.mipmap.flag);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            markerAtCenterOfMapIV.setImageTintList(ColorStateList.valueOf(Color.parseColor("#426E86")));
        }
    }

    /**
     * Once both source and destination
     * have been entered make the marker
     * and button at the center of the
     * screen disappear
     */
    void makeMarkerDisappear(){
        markerButton.setVisibility(View.INVISIBLE);
        markerAtCenterOfMapIV.setVisibility(View.INVISIBLE);
    }

    void makeSourceBarGreen(){
        searchSouceCardView.setCardBackgroundColor(Color.parseColor("#ccF9BA32"));
        searchSouceCardView.setCardElevation(getPixelsFromDPs(1));
    }

    void makeDestinationBarBlue(){
        searchDestinationCardView.setCardElevation(getPixelsFromDPs((float) 1));
        searchDestinationCardView.setCardBackgroundColor(Color.parseColor("#cc426E86"));
    }

    /**
     * Highlight the source bar
     */
    void elevateSourceBar(){
        searchSouceCardView.setCardBackgroundColor(Color.parseColor("#ffffff"));
        searchSouceCardView.setCardElevation(getPixelsFromDPs((6)));
    }

    /**
     * Highlight the destinaton bar
     */
    void elevateDestinationBar(){
        searchDestinationCardView.setCardElevation(getPixelsFromDPs((float) 6));
        searchDestinationCardView.setCardBackgroundColor(Color.parseColor("#ffffff"));
    }

    /**
     * Revert to initial non-colored
     * non-highlighted destination
     * bar
     */
    void makeDestinationBarBlank(){
        searchDestinationCardView.setCardElevation(getPixelsFromDPs((float) 1));
        searchDestinationCardView.setCardBackgroundColor(Color.parseColor("#aaffffff"));
    }

    ////////////////</Manipulate View>///////////////////////////////////////////////

    ////////////////<Custom Animations>///////////////////////

    /**
     * @param message: The string to show in the toast
     *
     *          Show a custom blue toast that notifies
     *          the user of what type of ride they
     *          have selected
     */
    void animateSelectionToast(String message) {
        if(noOfAnimationsRunning>=1){
            noOfAnimationsRunning--;
        }

        notifySelectionToastTV.setText(message);
        Animations.makeVisible(notifySelectionToastTV);
        if(!movingDownAnimation) {
            YoYo.with(Techniques.SlideInUp).listen(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    movingUpAnimation = true;
                    noOfAnimationsRunning++;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    movingUpAnimation = false;
                }
            }).duration(500).playOn(notifySelectionToastTV);
        }
        if(noOfAnimationsRunning == 1) {
            YoYo.with(Techniques.SlideOutDown).listen(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    movingDownAnimation = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    movingDownAnimation = false;
                    if(noOfAnimationsRunning>0){
                        noOfAnimationsRunning--;
                    }
                }
            }).duration(500).delay(2500).playOn(notifySelectionToastTV);

        }
    }

    /**
     * @param up: whether the request cab interface should be moved up or down
     *
     *          If the user has entered both source and destination move
     *          the request cab interface up so that the user can request
     *          thier cab.
     *
     *          Once any among the source and destination is missing
     *          move the request cab interface down so that the user
     *          enters both the source and destination before requesting
     *          the cab
     */
    void animateRequestCabView(boolean up){
        Techniques technique;
        if(up){
            Animations.makeVisible(requestCabButton,fairQuoteTV);
            Animations.playYoYoAnimOnMultipleViews(Techniques.SlideInUp,1000,requestCabButton,fairQuoteTV);
        }else{
            Animations.makeVisible(fairQuoteTV,requestCabButton);
            Animations.playYoYoAnimOnMultipleViews(Techniques.SlideOutDown,1000,fairQuoteTV,requestCabButton);
        }

    }

    void animateSourceDestinatonSelectionLayoutUp(){
        YoYo.with(Techniques.SlideOutUp).duration(1000).playOn(sourceDestinationSelectionLayoutCV);
    }

    void animateCarTypeSelectionAndRequestLayoutDown(){
        YoYo.with(Techniques.SlideOutDown).duration(1000).playOn(cabSelectionCV);
        YoYo.with(Techniques.SlideOutDown).duration(1000).playOn(notifySelectionToastTV);
        YoYo.with(Techniques.SlideOutDown).duration(1000).playOn(fairQuoteTV);
        YoYo.with(Techniques.SlideOutDown).duration(1000).playOn(requestCabButton);
    }
    ////////////////</Custom Animations>//////////////////////

    //////////////<Util Functions>////////////////////////////////////////////////////
    void getLocationPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_BLUETOOTH is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        View v = findViewById(R.id.map);
        View v1 = findViewById(R.id.source_destination_selection_layout);
        if ( v != null && v1!=null ) {
            int paddingTop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? getStatusBarHeight() : 0;
            RelativeLayout.LayoutParams mapLayoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
            RelativeLayout.LayoutParams searchCardLayoutParams = (RelativeLayout.LayoutParams) v1.getLayoutParams();
            mapLayoutParams.topMargin -= paddingTop;
            searchCardLayoutParams.topMargin += paddingTop;
            int paddingLeftOfETATV = etaOfCabTV.getPaddingLeft();
            int paddingRightOfETATV = etaOfCabTV.getPaddingRight();
            int paddingTopOfETATV = etaOfCabTV.getPaddingTop();
            int paddingBottomOfETATV = etaOfCabTV.getPaddingBottom();

            paddingTopOfETATV += getStatusBarHeight();
            etaOfCabTV.setPadding(
                    paddingLeftOfETATV,
                    paddingTopOfETATV,
                    paddingRightOfETATV,
                    paddingBottomOfETATV
            );
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

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected int getPixelsFromDPs(float dps){
        Resources r = getResources();
        int  px = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dps, r.getDisplayMetrics()));
        return px;
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
    //////////////</Util Functions>///////////////////////////////////////////////////
    ////////////////////////<Asynctask to send notification////////////////////////
    public class SendNotif extends AsyncTask<Integer,String,Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            if(params[0] == Constants.CANCEL_TRIP){
                sendCancelltripNotif();
            }
            return null;
        }
    }

    private void sendCancelltripNotif() {
        try {
            //URL url = new URL();
            //connection.setRequestMethod("POST");
            //connection.setRequestProperty();
            //connection.setRequestProperty();
            JSONObject jsonObject = new JSONObject();
            JSONObject dataObject = new JSONObject();
            dataObject.put("title","Cab Found");
            dataObject.put("body",  "The trip has been cancelled by your customer. Please wait for another");
            dataObject.put("driverId",getUid());
            jsonObject.put("notification",dataObject);

            //jsonObject.put("to",instanceId);
            jsonObject.put("to", instanceId);
            String jsonString = jsonObject.toString();
            Log.v("String json",jsonObject.toString());
            //connection.setDoInput(true);
            //connection.setDoOutput(true);

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("https://fcm.googleapis.com/fcm/send");
            StringEntity se = new StringEntity(jsonString);
            httpPost.setEntity(se);
            httpPost.addHeader("Content-Type","application/json");
            httpPost.addHeader("Authorization","key="+getString(R.string.fcm_auth_key));
            HttpResponse response = httpClient.execute(httpPost);
            Log.v("Respose ", response.getParams().toString());
            //Log.v("Response","response message: "+connection.getResponseMessage()+":: response code: "+connection.getResponseCode());

            Log.v("Notif", " CabFound notification sent");
            Log.v("InstanceId", instanceId);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

        }
    }

}
