package com.akhgupta.easylocation;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class LocationBgService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
    private static final long NO_FALLBACK = 0;
    private final String TAG = LocationBgService.class.getSimpleName();
    private GoogleApiClient googleApiClient;
    private int mLocationMode;
    private LocationRequest mLocationRequest;
    private Handler handler;
    private long fallBackToLastLocationTime;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.d(TAG,"googleApiClient created");
        googleApiClient.connect();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        Log.d(TAG,"googleApiClient start command "+ intent.getAction());
        if(intent.getAction().equals(AppConstants.ACTION_LOCATION_FETCH_START)) {
            mLocationMode = intent.getIntExtra(IntentKey.LOCATION_FETCH_MODE, AppConstants.SINGLE_FIX);
            mLocationRequest = intent.getParcelableExtra(IntentKey.LOCATION_REQUEST);
            fallBackToLastLocationTime = intent.getLongExtra(IntentKey.FALLBACK_TO_LAST_LOCATION_TIME,NO_FALLBACK);
            if (mLocationRequest == null)
                throw new IllegalStateException("Location request can't be null");
            if(googleApiClient.isConnected())
                requestLocationUpdates();
        }
        else if(intent.getAction().equals(AppConstants.ACTION_LOCATION_FETCH_STOP)) {
            stopLocationService();
        }
        return START_NOT_STICKY;
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdates() {
        if (mLocationRequest != null) {
            startFallbackToLastLocationTimer();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startFallbackToLastLocationTimer() {
        if(fallBackToLastLocationTime!=NO_FALLBACK) {
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
                }
            }, fallBackToLastLocationTime);
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG,"googleApiClient connected");
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"googleApiClient connection suspended");
        stopLocationService();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"googleApiClient connection failed");
        stopLocationService();
    }

    private void stopLocationService() {
        if(handler!=null)
            handler.removeCallbacksAndMessages(null);

        Log.d(TAG,"googleApiClient removing location updates");
        if(googleApiClient!=null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
            Log.d(TAG,"googleApiClient disconnect");
            googleApiClient.disconnect();
        }
        Log.d(TAG,"googleApiClient stop service");
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"googleApiClient location received");
        if(location!=null) {
            PreferenceUtil.getInstance(this).saveLastKnownLocation(location);
            Intent intent = new Intent();
            intent.setAction(AppConstants.INTENT_LOCATION_RECEIVED);
            intent.putExtra(IntentKey.LOCATION,location);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.setAction(AppConstants.INTENT_NO_LOCATION_RECEIVED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        if(mLocationMode == AppConstants.SINGLE_FIX)
            stopLocationService();
    }
}