# Android-EasyLocation

Getting location updates requires lots of bolierplate code in Android, You need to take care of
- Google Play services availablity Check, Update Google play Service Dialog
- Creation of GoogleApiClient and its callbacks connected,disconnected etc.
- Stopping and releasing resources for location updates
- Handling Location permission scenarios
- Checking Location services are On or Off
- Getting lastknown location is not so easy either
- Fallback to last known location if not getting location after certain duration

**Android-EasyLocation** does all this stuff in background, so that you can concentrate on your business logic than handling all above

## Getting started

In your `build.gradle`:

**com.google.android.gms:play-services-location** dependency also needs to be added like this

**x.x.x** can be replaced with google play service version your app is using [versions information available here](https://developers.google.com/android/guides/releases)

```gradle
 dependencies {
    compile 'com.akhgupta:android-easylocation:1.0.1'
    compile "com.google.android.gms:play-services-location:x.x.x"
 }
```

Extend your `Activity` from `EasyLocationAppCompatActivity` or `EasyLocationActivity`:  
(to run without an `Activity`, see below)

*Create location request according to your needs*

```java
LocationRequest locationRequest = new LocationRequest()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setInterval(5000)
        .setFastestInterval(5000);
```                        
*Create EasyLocation request, and set locationRequest created*
```java
EasyLocationRequest easyLocationRequest = new EasyLocationRequestBuilder()
        .setLocationRequest(locationRequest)
        .setFallBackToLastLocationTime(3000)
        .build();
}
```
**Request Single location update like this**
```java
requestSingleLocationFix(easyLocationRequest);
```
**Or Request Multiple location updates like this**
```java
requestLocationUpdates(easyLocationRequest);
```

**You're good to go!**, You will get below callbacks now in your activity

```java
    @Override
    public void onLocationPermissionGranted() {
    }

    @Override
    public void onLocationPermissionDenied() {
    }

    @Override
    public void onLocationReceived(Location location) {
    }

    @Override
    public void onLocationProviderEnabled() {
    }

    @Override
    public void onLocationProviderDisabled() {
    }
```

**Additional Options**

Specify what messages you want to show to user using *EasyLocationRequestBuilder*
```java
EasyLocationRequest easyLocationRequest = new EasyLocationRequestBuilder()
.setLocationRequest(locationRequest)
.setLocationPermissionDialogTitle(getString(R.string.location_permission_dialog_title))
.setLocationPermissionDialogMessage(getString(R.string.location_permission_dialog_message))
.setLocationPermissionDialogNegativeButtonText(getString(R.string.not_now))
.setLocationPermissionDialogPositiveButtonText(getString(R.string.yes))
.setLocationSettingsDialogTitle(getString(R.string.location_services_off))
.setLocationSettingsDialogMessage(getString(R.string.open_location_settings))
.setLocationSettingsDialogNegativeButtonText(getString(R.string.not_now))
.setLocationSettingsDialogPositiveButtonText(getString(R.string.yes))
.build();
```

## Running Without `Activity`

If you want to use this library in a `Service` or the similar, you may not have an `Activity` to use. In that case, you can use this library as follows.

First, you'll want to load the EasyLocationClientFactoryLoader, like so:
```java
EasyLocationClientFactoryLoader clientFactoryLoader = new EasyLocationClientFactoryLoader() {
    @Override
    public void onLocationProviderRequired() {
        // Neither GPS nor Network location providers are available
    }

    @Override
    public void onLocationPermissionRequired() {
        // The user hasn't provided the app with the ACCESS_FINE_LOCATION permission
    }

    @Override
    public void onGooglePlayServicesRequired() {
        // Google Play Services is unavailable
    }

    @Override
    public void onLoad(EasyLocationClientFactory clientFactory) {
        // Everything went fine - you now have access to a ClientFactory.
        // Perhaps assign it to an instance variable of your service or something:
        easyLocationClientFactory = clientFactory;
    }
};
```

Now you just have to use this loader in a call to `EasyLocationClientFactory.load`:

```java
EasyLocationClientFactory.load(this, clientFactoryLoader);
```

**Note:** When `load` is called, your loader will execute one of the four above methods. If it's not `onLoad`, then your loader will call one of the others to alert you of why it couldn't load for you. It's up to you to call `.load` again once you resolve whatever issue it alerts you of.

Now that you have a `EasyLocationClientFactory`, you can create clients and have them start listening for locations. (In order to do this, you'll need an `EasyLocationRequest` - *see above*):

```java
easyLocationClientFactory
.getContinuousUpdatesClient(easyLocationRequest)
.listen(new LocationBroadcastReceiver() {
    @Override
    public void onLocationReceived(Location location) {
        // do something with the location
    }
});
```

**Note:** If you only need a single update, use the `getSingleFixClient` method instead.


## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
