package com.navisens.demo.android_helloworld_kotlin
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.navisens.motiondnaapi.MotionDna
import com.navisens.motiondnaapi.MotionDnaSDK
import com.navisens.motiondnaapi.MotionDnaSDKListener
import java.util.*

/*
 * For complete documentation on the MotionDnaSDK API
 * Please go to the following link:
 * https://github.com/navisens/NaviDocs/blob/master/API.Android.md
 */

class MainActivity : AppCompatActivity(), MotionDnaSDKListener {

    internal lateinit var motionDnaSDK: MotionDnaSDK
    internal lateinit var receiveMotionDnaTextView: TextView
    internal lateinit var reportStatusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        receiveMotionDnaTextView = findViewById(R.id.receiveMotionDnaTextView)
        reportStatusTextView = findViewById(R.id.reportStatusTextView)

        // Requests app
        ActivityCompat.requestPermissions(this, MotionDnaSDK.getRequiredPermissions(), REQUEST_MDNA_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        startDemo()
    }

    fun startDemo() {
        val devKey = "<--DEVELOPER-KEY-HERE-->"

        motionDnaSDK = MotionDnaSDK(this.applicationContext, this)
        motionDnaSDK.startForegroundService()
        //    This functions starts up the SDK. You must pass in a valid developer's key in order for
        //    the SDK to function.
        motionDnaSDK.start(devKey)

    }

    //    This event receives the estimation results using a MotionDna object.
    //    Check out the Getters section to learn how to read data out of this object.

    override fun receiveMotionDna(motionDna: MotionDna) {
        var str = "Navisens MotionDnaSDK Estimation:\n"
        str += MotionDnaSDK.checkSDKVersion() + "\n"
        str += "Lat: " + motionDna.location.global.latitude + " Lon: " + motionDna.location.global.longitude + "\n"
        val location = motionDna.location.cartesian
        str += "Local XYZ Coordinates (meters)\n"
        str += String.format(" (%.2f, %.2f, %.2f)\n", location.x, location.y, location.z)
        str += String.format("Heading: %.3f\n", motionDna.location.global.heading)
        str += "motionType: " + motionDna.classifiers.get("motion")?.prediction?.label + "\n"
        str += "Predictions (BETA): \n\n"
        val classifiers = motionDna.classifiers
        for ((key, value) in classifiers) {
            str += String.format("Classifier: %s\n", key)
            str += String.format("\tcurrent prediction: %s confidence: %.2f\n", value.prediction.label, value.prediction.confidence)
            str += "\tprediction stats:\n"
            for ((key1, value1) in value.statistics) {
                str += String.format("\t%s", key1)
                str += String.format("\t duration: %.2f\n", value1.duration)
                str += String.format("\t distance: %.2f\n", value1.distance)
            }
            str += "\n"
        }

        val fstr = str
        runOnUiThread { receiveMotionDnaTextView.text = fstr }
    }

    //    Report SDK status updates

    override fun reportStatus(status: MotionDnaSDK.Status?, message: String?) {
        runOnUiThread { reportStatusTextView.append(String.format(Locale.US, "Status: %s Message: %s\n", status.toString(), message)) }
        when (status) {
            MotionDnaSDK.Status.AuthenticationFailure -> println("Status: ${MotionDnaSDK.Status.AuthenticationFailure.name} message: $message")
            MotionDnaSDK.Status.AuthenticationSuccess -> println("Status: ${MotionDnaSDK.Status.AuthenticationSuccess.name} message: $message")
            MotionDnaSDK.Status.Configuration -> println("Status: ${MotionDnaSDK.Status.Configuration.name} message: $message")
            MotionDnaSDK.Status.ExpiredSDK -> println("Status: ${MotionDnaSDK.Status.ExpiredSDK.name} message: $message")
            MotionDnaSDK.Status.MissingSensor -> println("Status: ${MotionDnaSDK.Status.MissingSensor.name} message: $message")
            MotionDnaSDK.Status.None -> println("Status: ${MotionDnaSDK.Status.None.name} message: $message")
            MotionDnaSDK.Status.PermissionsFailure -> println("Status: ${MotionDnaSDK.Status.PermissionsFailure.name} message: $message")
            MotionDnaSDK.Status.SensorTimingIssue -> println("Status: ${MotionDnaSDK.Status.SensorTimingIssue.name} message: $message")
        }
    }

    companion object {
        private val REQUEST_MDNA_PERMISSIONS = 1
    }
}
