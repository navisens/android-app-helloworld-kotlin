# android-app-helloworld-kotlin
An example Kotlin Android project using the Navisens MotionDNA SDK

## What it does
This project builds and runs a bare bones implementation of our SDK core. 

The core is on startup, triggering a call to the ```startMotionDna()``` method in the MainActivity.java. After this occurs the activity checks for necessary location permission and if requirements are satisfied, begins to receive Navisens MotionDNA location estimates through the ```receiveMotionDna()``` callback method. The data received is used to update the appropriate TextView element with a user's relative x,y and z coordinated along with GPS data and motion categorizations.

If multiple devices are running the app with the same developer key and have and active network connection, their device type and delative xyz coordinates will be listed at the bottom of the screen.

Before attempting to run this project please be sure to obtain a develepment key from Navisens. A key may be acquired free for testing purposes at [this link](https://navisens.com/index.html#contact)

For more complete documentation on our SDK please visit our [NaviDocs](https://github.com/navisens/NaviDocs)

___Note: This app is designed to run on Android 4.1 or higher___


## Setup

Enter your developer key in `app/src/main/java/com/navisens/demo/android_app_helloworld_kotlin/MainActivity.kt` and run the app.
```kotlin
fun startMotionDna() {
        val devKey = "<ENTER YOUR DEV KEY HERE>"
```

Walk around and see your position.

## How the SDK works

Please refer to our [NaviDoc](https://github.com/navisens/NaviDocs/blob/master/API.Android.md#api) for full documentation.

Here's some quick explaination for how to use our SDK in [android-app-helloworld](https://github.com/navisens/android-app-helloworld):

Add `implementation group: "com.navisens", name: "motiondnaapi", version: "1.7.1", changing: true` into dependencies section in `app/build.gradle` file to use our SDK.

In our SDK we provide `MotionDnaApplication` class and `MotionDnaInterface` interface. In order for MotionDna to work, we need a class implements all callback methods in the interface.  
In [android-app-helloworld](https://github.com/navisens/android-app-helloworld) it looks like this  
`class MainActivity : AppCompatActivity(), MotionDnaInterface`

In callback function we return `MotionDna` which contains [location, heading and motion type](https://github.com/navisens/NaviDocs/blob/master/API.Android.md#getters). Let's print it out.
```kotlin
override fun receiveMotionDna(motionDna: MotionDna)
    {
        var str = "Navisens MotionDna Location Data:\n"
        str += "Lat: " + motionDna.location.globalLocation.latitude + " Lon: " + motionDna.location.globalLocation.longitude + "\n"
        val location = motionDna.location.localLocation
        str += String.format(" (%.2f, %.2f, %.2f)\n",location.x, location.y, location.z)
        str += String.format("Heading: %.3f\n", motionDna.location.heading)
        str += "motionType: " + motionDna.motion.motionType + "\n"
        ...
```

Declare, and pass the class which implements `MotionDnaInterface`  
```kotlin
// Class instance variable declaration
internal lateinit var motionDnaApplication: MotionDnaApplication

// Instantiation within startMotionDna() or other startup method
motionDnaApplication = MotionDnaApplication(this);
```

Run MotionDna  
```kotlin
motionDnaApplication.runMotionDna(devKey);
```

Add some configurations  
```kotlin
motionDnaApplication.setLocationNavisens();
motionDnaApplication.setCallbackUpdateRateInMs(500);
```

More configurations are listed [here](https://github.com/navisens/NaviDocs/blob/master/API.Android.md#control)
