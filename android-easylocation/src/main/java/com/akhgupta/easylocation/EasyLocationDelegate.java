package com.akhgupta.easylocation;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.google.android.gms.common.GoogleApiAvailability;

class EasyLocationDelegate {
    private static final int PERMISSIONS_REQUEST = 100;
    private static final int ENABLE_LOCATION_SERVICES_REQUEST = 101;
    private static final int GOOGLE_PLAY_SERVICES_ERROR_DIALOG = 102;

    private final Activity activity;
    private EasyLocationClientFactory easyLocationClientFactory;
    private EasyLocationClient easyLocationClient;
    private EasyLocationClientFactoryLoader easyLocationClientFactoryLoader;
    private EasyLocationListener easyLocationListener;

    EasyLocationDelegate(final Activity activity, EasyLocationListener easyLocationListener) {
        this.activity = activity;
        this.easyLocationListener = easyLocationListener;
        easyLocationClientFactoryLoader = new EasyLocationClientFactoryLoader() {
            @Override
            public void onLocationProviderRequired() {
                showLocationServicesRequireDialog();
            }

            @Override
            public void onLocationPermissionRequired() {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION))
                    showPermissionRequireDialog();
                else
                    requestPermission();
            }

            @Override
            public void onGooglePlayServicesRequired() {
                showGooglePlayServicesErrorDialog();
            }

            @Override
            public void onLoad(EasyLocationClientFactory clientFactory) {
                easyLocationClientFactory = clientFactory;
            }
        };
        EasyLocationClientFactory.load(activity, easyLocationClientFactoryLoader);
    }

    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(intent, ENABLE_LOCATION_SERVICES_REQUEST);
    }

    void stopLocationUpdates() {
        EasyLocationClientFactory.stopService(activity);
    }

    private void showPermissionRequireDialog() {
        String title = TextUtils.isEmpty(easyLocationClient.getRequest().locationPermissionDialogTitle) ? activity.getString(R.string.location_permission_dialog_title) : easyLocationClient.getRequest().locationPermissionDialogTitle;
        String message = TextUtils.isEmpty(easyLocationClient.getRequest().locationPermissionDialogMessage) ? activity.getString(R.string.location_permission_dialog_message) : easyLocationClient.getRequest().locationPermissionDialogMessage;
        String negativeButtonTitle = TextUtils.isEmpty(easyLocationClient.getRequest().locationPermissionDialogNegativeButtonText) ? activity.getString(android.R.string.cancel) : easyLocationClient.getRequest().locationPermissionDialogNegativeButtonText;
        String positiveButtonTitle = TextUtils.isEmpty(easyLocationClient.getRequest().locationPermissionDialogPositiveButtonText) ? activity.getString(android.R.string.ok) : easyLocationClient.getRequest().locationPermissionDialogPositiveButtonText;
        new AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(negativeButtonTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        easyLocationListener.onLocationPermissionDenied();
                    }
                })
                .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermission();
                    }
                }).create().show();
    }

    private void showLocationServicesRequireDialog() {
        String title = TextUtils.isEmpty(easyLocationClient.getRequest().locationSettingsDialogTitle) ? activity.getString(R.string.location_services_off) : easyLocationClient.getRequest().locationSettingsDialogTitle;
        String message = TextUtils.isEmpty(easyLocationClient.getRequest().locationSettingsDialogMessage) ? activity.getString(R.string.open_location_settings) : easyLocationClient.getRequest().locationSettingsDialogMessage;
        String negativeButtonText = TextUtils.isEmpty(easyLocationClient.getRequest().locationSettingsDialogNegativeButtonText) ? activity.getString(android.R.string.cancel) : easyLocationClient.getRequest().locationSettingsDialogNegativeButtonText;
        String positiveButtonText = TextUtils.isEmpty(easyLocationClient.getRequest().locationSettingsDialogPositiveButtonText) ? activity.getString(android.R.string.ok) : easyLocationClient.getRequest().locationSettingsDialogPositiveButtonText;
        new AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        easyLocationListener.onLocationProviderDisabled();
                    }
                })
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openLocationSettings();
                    }
                })
                .create().show();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
    }

    private void showGooglePlayServicesErrorDialog() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (googleApiAvailability.isUserResolvableError(errorCode))
            googleApiAvailability.getErrorDialog(activity, errorCode, GOOGLE_PLAY_SERVICES_ERROR_DIALOG).show();
    }

    void onActivityResult(int requestCode) {
        switch (requestCode) {
            case ENABLE_LOCATION_SERVICES_REQUEST:
                LocationManager mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                boolean gpsLocationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean networkLocationEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (gpsLocationEnabled || networkLocationEnabled) {
                    easyLocationListener.onLocationProviderEnabled();
                    EasyLocationClientFactory.load(activity, easyLocationClientFactoryLoader);
                } else
                    easyLocationListener.onLocationProviderDisabled();
                break;
        }
    }

    void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    easyLocationListener.onLocationPermissionGranted();
                    EasyLocationClientFactory.load(activity, easyLocationClientFactoryLoader);
                } else
                    easyLocationListener.onLocationPermissionDenied();
                break;
        }
    }

    void onDestroy() {
        EasyLocationClientFactory.stopService(activity);
    }

    void requestLocationUpdates(EasyLocationRequest easyLocationRequest) {
        if(easyLocationClientFactory == null) {
            EasyLocationClientFactory.load(activity, easyLocationClientFactoryLoader);
            return;
        }
        easyLocationClient = easyLocationClientFactory
                .getContinuousUpdatesClient(easyLocationRequest)
                .listen(new LocationBroadcastReceiver() {
                    @Override
                    public void onLocationReceived(Location location) {
                        easyLocationListener.onLocationReceived(location);
                    }
                });

    }

    void requestSingleLocationFix(EasyLocationRequest easyLocationRequest) {
        if(easyLocationClientFactory == null) {
            EasyLocationClientFactory.load(activity, easyLocationClientFactoryLoader);
            return;
        }
        easyLocationClient = easyLocationClientFactory
                .getSingleFixClient(easyLocationRequest)
                .listen(new LocationBroadcastReceiver() {
                    @Override
                    public void onLocationReceived(Location location) {
                        easyLocationListener.onLocationReceived(location);
                    }
                });
    }

    public Location getLastKnownLocation() {
        return easyLocationClient.getLastKnownLocation();
    }
}
