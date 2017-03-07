package com.akhgupta.easylocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

public abstract class LocationBroadcastReceiver extends BroadcastReceiver {
    public abstract void onLocationReceived(Location location);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AppConstants.INTENT_LOCATION_RECEIVED)) {
            Location location = intent.getParcelableExtra(IntentKey.LOCATION);
            onLocationReceived(location);
        }
    }

}
