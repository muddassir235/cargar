package com.example.hp.thetacab.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.hp.thetacab.Activities.DriverMapActivity;
import com.example.hp.thetacab.Activities.MapsActivity;
import com.example.hp.thetacab.Activities.SignInActivity;
import com.example.hp.thetacab.R;
import com.example.hp.thetacab.tApplication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class StartupActivity extends AppCompatActivity {

    //APIs
    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Views
    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        setupFirebaseAuthentication();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    void setupFirebaseAuthentication() {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("UID-SharedPref",0);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("UID",user.getUid());
                    editor.commit();
                    FirebaseDatabase.getInstance().getReference().child("MapUIDtoInstanceID")
                            .child(user.getUid()).setValue(FirebaseInstanceId.getInstance().getToken());
                    ((tApplication)getApplication()).putUid(user.getUid());
                    Log.v("UID: ", user.getUid());

                    if(user.getUid() == null){
                        Log.v("StartUp: ", " user id is null");
                    }

                    FirebaseDatabase.getInstance().getReference().child("Customers").
                            child(user.getUid()).child("Type").
                            addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.v("StartUp: ","Entered OnDataChanged Listener");
                                    if (dataSnapshot.exists()) {
                                        Log.v("StartUp: ","Data Snapshot Exits");
                                        String customerType = (String) dataSnapshot.getValue();
                                        if (customerType.equals("Customer")) {
                                            Log.v("StartUp: ","Account Type Customer");
                                            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                            finish();
                                            startActivity(intent);
                                        } else if (customerType.equals("Driver")) {
                                            Log.v("StartUp: ","Account Type Driver");
                                            Intent intent = new Intent(getApplicationContext(), DriverMapActivity.class);
                                            finish();
                                            startActivity(intent);
                                        }
                                    }else{
                                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                                        finish();
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.v("StartUp: ","On Canceled");
                                }
                            });

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    Log.v("StartUp: ","User Signed Out");
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    startActivity(intent);
                }
                // ...
            }
        };
    }
}
