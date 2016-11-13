package org.c8.research.comming.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.dd.processbutton.iml.ActionProcessButton
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.mapbox.mapboxsdk.constants.MyBearingTracking
import com.mapbox.mapboxsdk.constants.MyLocationTracking
import com.pawegio.kandroid.e
import com.pawegio.kandroid.startActivity
import com.pawegio.kandroid.toast
import kotlinx.android.synthetic.main.activity_main.*
import org.c8.research.comming.CommingService
import org.c8.research.comming.Constants
import org.c8.research.comming.R
import org.c8.research.comming.entities.Api
import org.c8.research.comming.entities.Preferences
import org.c8.research.comming.services.LocationTrackingService
import org.c8.research.comming.utils.connectGoogleApi
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

const val PERMISSION_REQUEST_CODE: Int = 1342

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    val commingService: CommingService by lazy {
        CommingService.create(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPermissionsIfNeeded()

        // I hate those progress button libraries...
        share_location_button.setMode(ActionProcessButton.Mode.ENDLESS);
        share_location_button.setOnClickListener {
            share_location_button.progress = 1
            Observable.combineLatest(
                    commingService.createRoute(Api.NewRouteRequest("av1", "The Godzilla")),
                    connectGoogleApi(),
                    { a, b -> Pair(a, b)}
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        share_location_button.progress = 0

                        val ( route, googleApi ) = result
                        Preferences.Route.id = route.id
                        Preferences.Route.url = route.url
                        LocationTrackingService.startLocationTracking(this, googleApi)
                        googleApi.disconnect()

                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "text/plain"
                        intent.putExtra(Intent.EXTRA_SUBJECT, "I'm Coming")
                        intent.putExtra(Intent.EXTRA_TEXT, "I'M COMING FOR YOU. See for yourself:\n${route.url}")
                        startActivity(Intent.createChooser(intent, null))
                    }, { error ->
                        share_location_button.progress = 0
                        toast("Failed to start your session")
                        e("Failed to create a session $error")
                    })
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
            map.setOnMyLocationChangeListener {  }

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
            ActivityCompat.requestPermissions(this, arrayOf<String>(ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
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
                    getDrawable(Constants.AvatarsMap[Preferences.Settings.avatar]!!), null)
        }
    }
}
