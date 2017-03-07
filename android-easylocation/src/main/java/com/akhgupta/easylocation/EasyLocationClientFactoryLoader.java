package com.akhgupta.easylocation;

public interface EasyLocationClientFactoryLoader {
    void onLocationProviderRequired();
    void onLocationPermissionRequired();
    void onGooglePlayServicesRequired();
    void onLoad(EasyLocationClientFactory clientFactory);
}
