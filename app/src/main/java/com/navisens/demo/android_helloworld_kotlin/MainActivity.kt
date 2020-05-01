package com.navisens.demo.android_helloworld_kotlin

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock.elapsedRealtime
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.navisens.motiondnaapi.MotionDna
import com.navisens.motiondnaapi.MotionDnaSDK
import com.navisens.motiondnaapi.MotionDnaInterface
import java.util.*

/*
 * For complete documentation on Navisens SDK API
 * Please go to the following link:
 * https://github.com/navisens/NaviDocs/blob/master/API.Android.md
 */

class MainActivity : AppCompatActivity(), MotionDnaInterface {

    internal lateinit var motionDnaSDK: MotionDnaSDK
    internal var networkUsers = Hashtable<String, MotionDna>()
    internal var networkUsersTimestamps = Hashtable<String, Double>()
    internal lateinit var textView: TextView
    internal lateinit var networkTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.HELLO)
        networkTextView = findViewById(R.id.network)

        // Requests app
        ActivityCompat.requestPermissions(this, MotionDnaSDK.needsRequestingPermissions(), REQUEST_MDNA_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        startMotionDna()
    }

    fun startMotionDna() {
        val devKey = "<developer-key>"

        motionDnaSDK = MotionDnaSDK(this)

        //    This functions starts up the SDK. You must pass in a valid developer's key in order for
        //    the SDK to function. IF the key has expired or there are other errors, you may receive
        //    those errors through the reportError() callback route.

        motionDnaSDK.runMotionDna(devKey)

        //    Use our internal algorithm to automatically compute your location and heading by fusing
        //    inertial estimation with global location information. This is designed for outdoor use and
        //    will not compute a position when indoors. Solving location requires the user to be walking
        //    outdoors. Depending on the quality of the global location, this may only require as little
        //    as 10 meters of walking outdoors.

        motionDnaSDK.setLocationNavisens()

        //   Set accuracy for GPS positioning, states :HIGH/LOW_ACCURACY/OFF, OFF consumes
        //   the least battery.

        motionDnaSDK.setExternalPositioningState(MotionDna.ExternalPositioningState.LOW_ACCURACY)

        //    Manually sets the global latitude, longitude, and heading. This enables receiving a
        //    latitude and longitude instead of cartesian coordinates. Use this if you have other
        //    sources of information (for example, user-defined address), and need readings more
        //    accurate than GPS can provide.
        //        motionDnaSDK.setLocationLatitudeLongitudeAndHeadingInDegrees(37.787582, -122.396627, 0);

        //    Set the power consumption mode to trade off accuracy of predictions for power saving.

        motionDnaSDK.setPowerMode(MotionDna.PowerConsumptionMode.PERFORMANCE)

        //    Connect to your own server and specify a room. Any other device connected to the same room
        //    and also under the same developer will receive any udp packets this device sends.

        motionDnaSDK.startUDP()

        //    Allow our SDK to record data and use it to enhance our estimation system.
        //    Send this file to support@navisens.com if you have any issues with the estimation
        //    that you would like to have us analyze.

        motionDnaSDK.setBinaryFileLoggingEnabled(true)

        //    Tell our SDK how often to provide estimation results. Note that there is a limit on how
        //    fast our SDK can provide results, but usually setting a slower update rate improves results.
        //    Setting the rate to 0ms will output estimation results at our maximum rate.

        motionDnaSDK.setCallbackUpdateRateInMs(500.0)

        //    When setLocationNavisens is enabled and setBackpropagationEnabled is called, once Navisens
        //    has initialized you will not only get the current position, but also a set of latitude
        //    longitude coordinates which lead back to the start position (where the SDK/App was started).
        //    This is useful to determine which building and even where inside a building the
        //    person started, or where the person exited a vehicle (e.g. the vehicle parking spot or the
        //    location of a drop-off).
        motionDnaSDK.setBackpropagationEnabled(true)

        //    If the user wants to see everything that happened before Navisens found an initial
        //    position, he can adjust the amount of the trajectory to see before the initial
        //    position was set automatically.
        motionDnaSDK.setBackpropagationBufferSize(2000)

        //    Enables AR mode. AR mode publishes orientation quaternion at a higher rate.

        //        motionDnaSDK.setARModeEnabled(true);
    }

    //    This event receives the estimation results using a MotionDna object.
    //    Check out the Getters section to learn how to read data out of this object.

    override fun receiveMotionDna(motionDna: MotionDna) {
        var str = "Navisens MotionDna Location Data:\n"
        str += MotionDnaSDK.checkSDKVersion() + "\n"
        str += "Lat: " + motionDna.location.globalLocation.latitude + " Lon: " + motionDna.location.globalLocation.longitude + "\n"
        val location = motionDna.location.localLocation
        str += "Local XYZ Coordinates (meters)\n"
        str += String.format(" (%.2f, %.2f, %.2f)\n", location.x, location.y, location.z)
        str += String.format("Heading: %.3f\n", motionDna.location.heading)
        str += "motionType: " + motionDna.motion.motionType + "\n"
        textView.setTextColor(Color.BLACK)

        str += "Predictions (BETA): \n\n"
        val classifiers = motionDna.classifiers
        for ((key, value) in classifiers) {
            str += String.format("Classifier: %s\n", key)
            str += String.format("\tcurrent prediction: %s confidence: %.2f\n", value.currentPredictionLabel, value.currentPredictionConfidence)
            str += "\tprediction stats:\n"
            for ((key1, value1) in value.predictionStatsMap) {
                str += String.format("\t%s", key1)
                str += String.format("\t duration: %.2f\n", value1.duration)
                str += String.format("\t distance: %.2f\n", value1.distance)
            }
            str += "\n"
        }

        val fstr = str
        runOnUiThread { textView.text = fstr }
    }

    //    This event receives estimation results from other devices in the server room. In order
    //    to receive anything, make sure you call startUDP to connect to a room. Again, it provides
    //    access to a MotionDna object, which can be unpacked the same way as above.
    //
    //
    //    If you aren't receiving anything, then the room may be full, or there may be an error in
    //    your connection. See the reportError event below for more information.

    override fun receiveNetworkData(motionDna: MotionDna) {

        networkUsers[motionDna.id] = motionDna
        val timeSinceBootSeconds = elapsedRealtime() / 1000.0
        networkUsersTimestamps[motionDna.id] = timeSinceBootSeconds
        val activeNetworkUsersStringBuilder = StringBuilder()
        val toRemove = ArrayList<String>()

        activeNetworkUsersStringBuilder.append("Network Shared Devices:\n")
        for (user in networkUsers.values) {
            if (timeSinceBootSeconds - networkUsersTimestamps[user.id] as Double > 2.0) {
                toRemove.add(user.id)
            } else {
                activeNetworkUsersStringBuilder.append(user.deviceName.split(";").last())
                val location = user.location.localLocation
                activeNetworkUsersStringBuilder.append(String.format(" (%.2f, %.2f, %.2f)", location.x, location.y, location.z))
                activeNetworkUsersStringBuilder.append("\n")
            }

        }
        for (key in toRemove) {
            networkUsers.remove(key)
            networkUsersTimestamps.remove(key)
        }

        runOnUiThread { networkTextView.text = activeNetworkUsersStringBuilder.toString() }
    }

    //    This event receives arbitrary data from the server room. You must have
    //    called startUDP already to connect to the room.

    override fun receiveNetworkData(networkCode: MotionDna.NetworkCode, map: Map<String, *>) {

    }

    //    Report any errors of the estimation or internal SDK

    override fun reportError(errorCode: MotionDna.ErrorCode, s: String) {
        when (errorCode) {
            MotionDna.ErrorCode.ERROR_AUTHENTICATION_FAILED -> println("Error: authentication failed $s")
            MotionDna.ErrorCode.ERROR_SDK_EXPIRED -> println("Error: SDK expired $s")
            MotionDna.ErrorCode.ERROR_WRONG_FLOOR_INPUT -> println("Error: wrong floor input $s")
            MotionDna.ErrorCode.ERROR_PERMISSIONS -> println("Error: permissions not granted $s")
            MotionDna.ErrorCode.ERROR_SENSOR_MISSING -> println("Error: sensor missing $s")
            MotionDna.ErrorCode.ERROR_SENSOR_TIMING -> println("Error: sensor timing $s")
        }
    }

    //    The two required methods shown below bind
    //    the interface to your application's activity,
    //    so MotionDna is able to retrieve the necessary
    //    permissions and capabilities
    override fun getPkgManager(): PackageManager {
        return packageManager
    }

    override fun getAppContext(): Context {
        return applicationContext
    }

    companion object {
        private val REQUEST_MDNA_PERMISSIONS = 1
    }
}
