package com.example.hp.thetacab.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.hp.thetacab.Activities.MapsActivity;
import com.example.hp.thetacab.FireBaseEntry;
import com.example.hp.thetacab.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TransitionActivity extends AppCompatActivity {
    ProgressDialog progress;
    String API;
    String newMail;
    ArrayList<String> newVariables;
    private DatabaseReference mDatabase;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);

        progress = ProgressDialog.show(this, "Authenitcating", "Please Wait ... ", true);
        Intent i = getIntent();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ArrayList<String> variables = i.getStringArrayListExtra("variables");
        newVariables = new ArrayList<String>();
        API = i.getStringExtra("loginActivity");

        newMail = parseEmail(variables.get(0));
        newVariables.add(newMail);
        for (int j = 1; j < variables.size(); j++) {
            newVariables.add(variables.get(j));

        }
        Log.e("newmail", newMail);
        //Verify whether User Exists or notchild("g!shar98@gmail!com").get
        mDatabase.child("users").child(newMail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                    i.putStringArrayListExtra("variables", newVariables);
                    i.putExtra("loginActivity", API);
                    Log.e("childexists", dataSnapshot.getKey().toString());
                    startActivity(i);

                } else {
                    Intent i = new Intent(getApplicationContext(), FireBaseEntry.class);
                    i.putStringArrayListExtra("variables", newVariables);
                    i.putExtra("loginActivity", API);
                    Log.e("childdoesnotexist", dataSnapshot.getKey().toString());
                    startActivity(i);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /////////////////////////////////////////
        //Verify Cell number and send it to database.


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public String parseEmail(String s) {
        String newEmail = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '.') {
                newEmail = newEmail + String.valueOf(s.charAt(i));
            } else {
                newEmail = newEmail + "!";
            }
        }
        return newEmail;

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Transition Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.hp.thetacab/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Transition Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.hp.thetacab/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
