# Android-EasyLocation

Getting location updates requires lots of bolierplate code in Android, You need to take care of 
- Google Play services availablity Check, Update Google play Service Dialog
- Creation of GoogleApiClient and its callbacks connected,disconnected etc.
- Stopping and releasing resources for location updates
- Handling Location permission scenarios
- Checking Location services are On or Off
- Getting lastknown location is not so easy either

**Android-EasyLocation** does all this stuff in background, so that you can concentrate on your business logic than handling all above

## Getting started

In your `build.gradle`:

```gradle
 dependencies {
    compile 'com.akhgupta:android-easylocation:0.8.0'
 }
```

Extend your `Activity` from `EasyLocationAppCompatActivity` or `EasyLocationActivity`:

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