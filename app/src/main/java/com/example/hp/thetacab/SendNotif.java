package com.example.hp.thetacab;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by hp on 7/15/2016.
 */
public class SendNotif {
    private ArrayList<OnDataRetrievedListener> dataRetrievedListeners;

    private Context context;
    private int direction;
    public static final int DRIVER_TO_PASSENGER = 0;
    public static final int PASSENGER_TO_DRIVER = 1;

    private int notifType;
    public static final int CAB_FOUND_NOTIF = 0;

    public static final int CAB_ARRIVED_NOTIF = 1;

    public static final int CAB_TRIP_STARTED = 2;

    public static final int CAB_REACHED_DESTINATION_NOTIF = 3;

    private String recieverId;

    private String recieverInstanceId;

    private String title;

    private String message;

    private interface OnDataRetrievedListener{
        void onDataRetrieved(String... data);
    }

    private void setOnDataRetrievedListener(OnDataRetrievedListener dataRetrievedListener){
        this.dataRetrievedListeners.add(dataRetrievedListener);
    }

    private void retriveData(){
        final String[] appStatus = new String[1];
        if(notifType == CAB_FOUND_NOTIF || notifType == CAB_ARRIVED_NOTIF){
            final String[] driverName = new String[1];
            FirebaseDatabase.getInstance().getReference().child("AppStatus").child(recieverId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        appStatus[0] =String.valueOf((Long) dataSnapshot.getValue());
                        if (Integer.valueOf(appStatus[0]) == 0) {
                            FirebaseDatabase.getInstance().getReference().child("Customers").child(getUid()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        driverName[0] = (String) dataSnapshot.getValue();
                                        FirebaseDatabase.getInstance().getReference().child("MapUIDtoInstanceID").child(recieverId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    recieverInstanceId = (String) dataSnapshot.getValue();
                                                    for(ListIterator<OnDataRetrievedListener> it = dataRetrievedListeners.listIterator(); it.hasNext(); ){
                                                        OnDataRetrievedListener aListener = it.next();
                                                        aListener.onDataRetrieved(driverName[0]);
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

                        } else {
                            // do nothing
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else if(notifType == CAB_TRIP_STARTED || notifType == CAB_REACHED_DESTINATION_NOTIF){
            FirebaseDatabase.getInstance().getReference().child("AppStatus").child(recieverId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        appStatus[0] = String.valueOf((Long) dataSnapshot.getValue());
                        if (Integer.valueOf(appStatus[0]) == 0) {
                                        Log.v("AppStatus", " The app is closed");
                                        FirebaseDatabase.getInstance().getReference().child("MapUIDtoInstanceID").child(recieverId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    Log.v("InstanceId", " the instance id has been retrieved.");
                                                    recieverInstanceId = (String) dataSnapshot.getValue();
                                                    for(ListIterator<OnDataRetrievedListener> it = dataRetrievedListeners.listIterator(); it.hasNext(); ){
                                                        OnDataRetrievedListener aListener = it.next();
                                                        aListener.onDataRetrieved("your destination");
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }
                        } else {
                            // do nothing
                        }
                    }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    public SendNotif(Context context,int direction, int notifType, String recieverId){
        dataRetrievedListeners = new ArrayList<>();
        this.context = context;
        this.direction = direction;
        this.notifType = notifType;
        this.recieverId = recieverId;
    }

    public SendNotif setTitle(String title){
        this.title = title;
        return this;
    }

    public String getTitle(){
        return this.title;
    }

    public String getMessage(){
        return this.message;
    }

    public SendNotif send() {
        if (direction == DRIVER_TO_PASSENGER) {
            FirebaseDatabase.getInstance().getReference().child("State").child(recieverId).setValue(3);
        }

        retriveData();
        setOnDataRetrievedListener(new OnDataRetrievedListener() {
            @Override
            public void onDataRetrieved(String... data) {
                SendNotification sendNotification = new SendNotification();
                switch (notifType){
                    case CAB_FOUND_NOTIF:message=data[0]+" is coming to pick you up.";break;
                    case CAB_ARRIVED_NOTIF:message="Your driver "+data[0]+" has arrived at the pick up point.";break;
                    case CAB_TRIP_STARTED:message="Your trip to "+data[0]+" has started";break;
                    case CAB_REACHED_DESTINATION_NOTIF:message="You have arrived at "+data[0];break;
                    default:message="No message";
                }
                sendNotification.execute(String.valueOf(notifType),recieverInstanceId,title,message);
            }
        });
        return this;
    }

    String getUid(){
        SharedPreferences sharedPref = context.getSharedPreferences("UID-SharedPref",0);
        String uid = sharedPref.getString("UID",null);
        Log.v("User Id: ", uid);
        return uid;
    }

    public class SendNotification extends AsyncTask<String,String,Void> {

        @Override
        protected Void doInBackground(String... params) {
            sendNofif(params[0],params[1],params[2],params[3]);
            return null;
        }
    }

    private void sendNofif(String notifType,String recieverInstanceId,String title, String message) {
        try {
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Authorization","key="+context.getString(R.string.fcm_auth_key));
            JSONObject jsonObject = new JSONObject();
            JSONObject dataObject = new JSONObject();
            dataObject.put("title",title);
            dataObject.put("body", message);
            dataObject.put("senderId",getUid());
            dataObject.put("notifType",notifType);
            jsonObject.put("notification",dataObject);

            jsonObject.put("to", recieverInstanceId);
            String jsonString = jsonObject.toString();
            Log.v("String json",jsonObject.toString());
            //connection.setDoInput(true);
            //connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonString);
            writer.flush();
            writer.close();
            os.close();

            connection.connect();
            Log.v("Response","response message: "+connection.getResponseMessage()+":: response code: "+connection.getResponseCode());

            Log.v("Notif", " CabFound notification sent");
            Log.v("InstanceId", recieverInstanceId);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
