package org.c8.research.comming.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.mapbox.mapboxsdk.constants.MyBearingTracking
import com.mapbox.mapboxsdk.constants.MyLocationTracking
import com.pawegio.kandroid.startActivity
import com.pawegio.kandroid.toast
import kotlinx.android.synthetic.main.activity_main.*
import org.c8.research.comming.Constants
import org.c8.research.comming.LocationBoard
import org.c8.research.comming.R
import org.c8.research.comming.entities.Preferences

const val PERMISSION_REQUEST_CODE: Int = 1342

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    val locationBoard by lazy {
        LocationBoard(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPermissionsIfNeeded()

        share_button.setOnClickListener {
            locationBoard.locationStatus
                    .doOnSubscribe { progressBar.visibility = View.VISIBLE }
                    .filter { status -> (status == LocationBoard.Status.RUNNING || status == LocationBoard.Status.ERROR) }
                    .first()
                    .subscribe({ status ->
                        progressBar.visibility = View.GONE
                        if (status == LocationBoard.Status.RUNNING) {
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = "text/plain"
                            intent.putExtra(Intent.EXTRA_SUBJECT, "I'm Coming")
                            intent.putExtra(Intent.EXTRA_TEXT, "I'M COMING FOR YOU. See for yourself:\n${Preferences.Route.url}")
                            startActivity(Intent.createChooser(intent, null))
                        } else {
                            Toast.makeText(this, "smthing wrong", Toast.LENGTH_SHORT).show()
                        }
                    })
            locationBoard.startLocationService(this)
        }

        map_view.onCreate(savedInstanceState)
        map_view.getMapAsync { map ->
            // We enable mapbox location tracking, but make our own marker (disabling existing one)
            map.trackingSettings.myLocationTrackingMode = MyLocationTracking.TRACKING_FOLLOW
            map.trackingSettings.myBearingTrackingMode = MyBearingTracking.NONE
            map.trackingSettings.isDismissLocationTrackingOnGesture = true
            map.myLocationViewSettings.accuracyAlpha = 0
            map.myLocationViewSettings.foregroundDrawable
            map.uiSettings.isRotateGesturesEnabled = false
            map.uiSettings.isTiltGesturesEnabled = false
            map.setOnMyLocationChangeListener { }

            map.setOnMapClickListener {
                startActivity<SettingsActivity>()
            }
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        toast("Sorry, google play services failed")
        finish()
    }

    override fun onConnectionSuspended(code: Int) {
        // Intentionally do nothing
    }

    override fun onConnected(bundle: Bundle?) {
        // Intentionally do nothing
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        map_view.onSaveInstanceState(outState!!)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        super.onResume()
        map_view.onResume()
        updateUserAvatar()
    }

    override fun onPause() {
        map_view.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        map_view.onDestroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return
        }
        if (grantResults.isEmpty() || grantResults.first() != PackageManager.PERMISSION_GRANTED) {
            // TODO: Redirect to error activity, explaining that we need location permission
            toast("Sorry, this app requires location permission to work")
            finish()
            return
        }
        startLocationServices()
    }

    fun askPermissionsIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            // Continue in @onRequestPermissionResult
        } else {
            startLocationServices()
        }
    }

    fun startLocationServices() {
        map_view.getMapAsync {
            // Enable location tracking for mapbox, after we ensure permissions
            it.isMyLocationEnabled = true
        }
    }

    fun updateUserAvatar() {
        map_view.getMapAsync {
            it.myLocationViewSettings.setForegroundDrawable(
                    ContextCompat.getDrawable(this, Constants.AvatarsMap[Preferences.Settings.avatar]!!), null)
        }
    }
}
