package com.akhgupta.easylocation;

import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;

public class EasyLocationClient {
    private Context context;
    private LocationBroadcastReceiver receiver;
    private EasyLocationRequest easyLocationRequest;

    EasyLocationClient(Context context, EasyLocationRequest easyLocationRequest){
        this.context = context;
        this.easyLocationRequest = easyLocationRequest;
    }

    public EasyLocationRequest getRequest(){
        return easyLocationRequest;
    }

    public EasyLocationClient listen(LocationBroadcastReceiver newReceiver){
        if(receiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        }

        receiver = newReceiver;

        // Attach location broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.INTENT_LOCATION_RECEIVED);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter);

        return this;
    }

    public Location getLastKnownLocation() {
        return PreferenceUtil.getInstance(context).getLastKnownLocation();
    }

    public EasyLocationClient stopListening() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        return this;
    }

}