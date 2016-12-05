package com.akhgupta.easylocation.demo;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.akhgupta.easylocation.EasyLocationAppCompatActivity;
import com.akhgupta.easylocation.EasyLocationRequest;
import com.akhgupta.easylocation.EasyLocationRequestBuilder;
import com.google.android.gms.location.LocationRequest;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends EasyLocationAppCompatActivity {
    @BindView(R.id.requestSingleLocationButton)
    Button requestSingleLocationButton;
    @BindView(R.id.requestLocationUpdatesButton)
    Button requestLocationUpdatesButton;
    @BindView(R.id.stopLocationUpdatesButton)
    Button stopLocationUpdatesButton;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    public void onLocationPermissionGranted() {
        showToast("Location permission granted");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationPermissionDenied() {
        showToast("Location permission denied");
    }

    @Override
    public void onLocationReceived(Location location) {
        showToast(location.getProvider() + "," + location.getLatitude() + "," + location.getLongitude());
    }

    @Override
    public void onLocationProviderEnabled() {
        showToast("Location services are now ON");
    }

    @Override
    public void onLocationProviderDisabled() {
        showToast("Location services are still Off");
    }

    @OnClick({R.id.requestSingleLocationButton, R.id.requestLocationUpdatesButton, R.id.stopLocationUpdatesButton})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.requestSingleLocationButton: {
                LocationRequest locationRequest = new LocationRequest()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(5000)
                        .setFastestInterval(5000);
                EasyLocationRequest easyLocationRequest = new EasyLocationRequestBuilder()
                        .setLocationRequest(locationRequest)
                        .setFallBackToLastLocationTime(3000)
                        .build();
                requestSingleLocationFix(easyLocationRequest);
            }
            break;
            case R.id.requestLocationUpdatesButton: {
                LocationRequest locationRequest = new LocationRequest()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(5000)
                        .setFastestInterval(5000);
                EasyLocationRequest easyLocationRequest = new EasyLocationRequestBuilder()
                        .setLocationRequest(locationRequest)
                        .setFallBackToLastLocationTime(3000)
                        .build();
                requestLocationUpdates(easyLocationRequest);
            }
            break;
            case R.id.stopLocationUpdatesButton:
                stopLocationUpdates();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}