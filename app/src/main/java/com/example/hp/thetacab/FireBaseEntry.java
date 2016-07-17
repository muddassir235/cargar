package com.example.hp.thetacab;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.hp.thetacab.Activities.MapsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FireBaseEntry extends AppCompatActivity {
    String Email;
    String Birthday;
    String Name;
    String Gender;
    String CNIC;

    EditText Cell_edit;
    TextView Cell_text;
    TextView CNIC_text;
    EditText CNIC_edit;
    TextView Gender_text;
    EditText Gender_edit;
    TextView Birthday_text;
    EditText Birthday_edit;
    RadioGroup radioid;
    RadioButton buttonId;
    String customerType;
    private DatabaseReference mDatabase;
    ArrayList<String> newVariables;
    String API;
    String newMail;
    String UID;
    ArrayList<String> variables;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    boolean closeActivity;

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_base_entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        variables=new ArrayList<String>();
        newVariables= new ArrayList<String>();
        Cell_edit=(EditText)findViewById(R.id.cell_phone_edit);
        CNIC_edit=(EditText) findViewById(R.id.cnic_edit);
        Gender_edit=(EditText)findViewById(R.id.gender_edit);
        Birthday_edit=(EditText)findViewById(R.id.birthday_edit);
        radioid=(RadioGroup)findViewById(R.id.customerid);
        mDatabase= FirebaseDatabase.getInstance().getReference();
        findViewById(R.id.okay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id= radioid.getCheckedRadioButtonId();
                String customerid=((RadioButton)findViewById(id)).getText().toString();

                mDatabase.child("users").child(newVariables.get(0)).setValue("1");
                if(customerType.compareTo("facebook")==0){
                    FinalUserObject user=new FinalUserObject(newVariables.get(0),newVariables.get(2),newVariables.get(1),newVariables.get(3),CNIC_edit.getText().toString(),Cell_edit.getText().toString(),customerid);
                    mDatabase.child("Customers").child(UID).setValue(user);
                    Intent i = new Intent(FireBaseEntry.this,MapsActivity.class);
                    startActivity(i);
                }
                if(customerType.compareTo("google")==0){
                    FinalUserObject user=new FinalUserObject(newVariables.get(0),newVariables.get(1),Birthday_edit.getText().toString(),Gender_edit.getText().toString(),CNIC_edit.getText().toString(),Cell_edit.getText().toString(),customerid);
                    mDatabase.child("Customers").child(UID).setValue(user);
                    Intent i = new Intent(FireBaseEntry.this,MapsActivity.class);
                    startActivity(i);
                }



            }
        });
        mAuth=FirebaseAuth.getInstance();
        closeActivity=false;
        //getting email and user id of user
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
  //                  Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());




                } else {
                    // User is signed out
    //                Log.d(TAG, "onAuthStateChanged:signed_out");

                }
                // ...
            }
        };
        ////////////////////////
        UID=((tApplication)getApplication()).getUid();
        if(getIntent().getExtras()!=null){
            closeActivity=false;
            Intent i=getIntent();
            API= i.getStringExtra("loginActivity");
            variables = i.getStringArrayListExtra("variables");
            newMail=parseEmail(variables.get(0));
            newVariables.add(newMail);
            for(int j = 1 ; j< variables.size();j++){
                newVariables.add(variables.get(j));
            }
            for(int j = 0 ; j < newVariables.size();j++) {
                Log.e("All Childeren"+Integer.valueOf(j), newVariables.get(j));
            }
            if(API.compareTo("google")==0){
                Google user= new Google(newVariables.get(0),newVariables.get(1));
                mDatabase.child("Customers").child(UID).setValue(user);
                customerType="google";

            }
            else if(API.compareTo("facebook")==0){
                Facebook user= new Facebook(newVariables.get(0),newVariables.get(1),newVariables.get(2),newVariables.get(3));
                mDatabase.child("Customers").child(UID).setValue(user);
                Birthday_text.setVisibility(View.GONE);
                Gender_text.setVisibility(View.GONE);
                Gender_edit.setVisibility(View.GONE);
                Birthday_edit.setVisibility(View.GONE);
                customerType="facebook";
            }

            //there is an error with facebook resolve it .
        }
        else{
            closeActivity=true;
            mDatabase.child("Customers").child(UID).child("type").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e("Just snaping",dataSnapshot.toString());
                    if(dataSnapshot.exists()){
                        String type=(String)dataSnapshot.getValue();
                        Log.e("type",type);
                        //do all your working here
                        customerType=type;
                        if(type.compareTo("facebook")==0){
                            Birthday_text.setVisibility(View.GONE);
                            Gender_text.setVisibility(View.GONE);
                            Gender_edit.setVisibility(View.GONE);
                            Birthday_edit.setVisibility(View.GONE);
                            //fetching more data
                            mDatabase.child("Customers").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot child : dataSnapshot.getChildren()){
                                        Log.e("snapshot values",child.getKey()+" : "+child.getValue());
                                        if(child.getKey().compareTo("type")!=0) {
                                            newVariables.add(child.getValue().toString());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                        if(type.compareTo("google")==0){
                            mDatabase.child("Customers").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot child : dataSnapshot.getChildren()){
                                        Log.e("snapshot values",child.getKey()+" : "+child.getValue());

                                        if(child.getKey().compareTo("type")!=0) {
                                            newVariables.add(child.getValue().toString());
                                        }
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

    }
    public String parseEmail(String s ){
        String newEmail="";
        for(int i = 0 ; i < s.length();i++){
            if(s.charAt(i)!='.'){
                newEmail=newEmail+String.valueOf(s.charAt(i));
            }
            else{
                newEmail=newEmail+"!";
            }
        }
        return newEmail;

    }

}
