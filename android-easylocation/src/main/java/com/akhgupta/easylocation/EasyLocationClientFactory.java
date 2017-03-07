package com.akhgupta.easylocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class EasyLocationClientFactory {
    private Context context;
    private static EasyLocationClient currentEasyLocationClient;

    private EasyLocationClientFactory(Context context) {
        this.context = context;
    }

    private static EasyLocationClientFactory easyLocationClientFactory;

    // Prepare client - verify all needed permissions and services are available
    public static void load(Context context, EasyLocationClientFactoryLoader clientFactoryLoader){
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsLocationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkLocationEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean locationEnabled = gpsLocationEnabled || networkLocationEnabled;
        boolean locationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean googleServiceAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;

        // If any permission or service is missing, have consumer handle it - else call onReady
        if(!locationEnabled){
            clientFactoryLoader.onLocationProviderRequired();
        }
        else if(!locationPermissionGranted){
            clientFactoryLoader.onLocationPermissionRequired();
        }
        else if(!googleServiceAvailable){
            clientFactoryLoader.onGooglePlayServicesRequired();
        }
        else {
            if(easyLocationClientFactory == null)
                easyLocationClientFactory = new EasyLocationClientFactory(context);
            clientFactoryLoader.onLoad(easyLocationClientFactory);
        }
    }

    public EasyLocationClient getSingleFixClient(EasyLocationRequest easyLocationRequest) {
        return getClient(easyLocationRequest, AppConstants.SINGLE_FIX);
    }
    public EasyLocationClient getContinuousUpdatesClient(EasyLocationRequest easyLocationRequest) {
        return getClient(easyLocationRequest, AppConstants.CONTINUOUS_LOCATION_UPDATES);
    }
    private EasyLocationClient getClient(EasyLocationRequest easyLocationRequest, int locationFetchMode) {
        if(currentEasyLocationClient != null)
            currentEasyLocationClient.stopListening();

        Intent intent = new Intent(context, LocationBgService.class);
        intent.setAction(AppConstants.ACTION_LOCATION_FETCH_START);
        intent.putExtra(IntentKey.LOCATION_REQUEST, easyLocationRequest.locationRequest);
        intent.putExtra(IntentKey.LOCATION_FETCH_MODE, locationFetchMode);
        intent.putExtra(IntentKey.FALLBACK_TO_LAST_LOCATION_TIME, easyLocationRequest.fallBackToLastLocationTime);
        context.startService(intent);

        currentEasyLocationClient = new EasyLocationClient(context, easyLocationRequest);
        return currentEasyLocationClient;
    }

    public void stopService(){
        stopService(context);
    }

    public static void stopService(Context context){
        if(currentEasyLocationClient != null)
            currentEasyLocationClient.stopListening();
        currentEasyLocationClient = null;
        Intent intent = new Intent(context, LocationBgService.class);
        intent.setAction(AppConstants.ACTION_LOCATION_FETCH_STOP);
        context.startService(intent);
    }

}
