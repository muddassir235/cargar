package com.example.hp.thetacab;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by gul on 6/20/16.
 */
public class tApplication extends MultiDexApplication {
    private String uid;
    public boolean authStateListenerNotActive;
    @Override
    public void onCreate()
    {
        super.onCreate();
        authStateListenerNotActive = false;
        // Initialize the singletons so their instances
        // are bound to the application process.

    }


    public void putUid(String S)
    {
        uid=S;
    }
    public String getUid(){
        return uid;
    }
}
