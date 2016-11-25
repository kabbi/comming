package org.c8.research.comming.services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
import org.c8.research.comming.ImComingApplication
import org.c8.research.comming.LocationBoard
import org.c8.research.comming.R
import org.c8.research.comming.entities.Api
import org.c8.research.comming.entities.Preferences
import org.c8.research.comming.utils.connectGoogleApi
import rx.schedulers.Schedulers


const val LOCATION_UPDATE_INTERVAL_MILLIS: Long = 1000
const val MIN_UPDATE_INTERVAL_MILLIS: Long = 1000

class LocationTrackingService : Service(), LocationListener {

    val locationBoard by lazy { (application as ImComingApplication).locationBoard }
    val comingApi by lazy { (application as ImComingApplication).comingApi }

    var googleApiClient: GoogleApiClient? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val action = intent!!.action
        if (ACTION_START_LOCATION == action) {

            //todo need normal check os location status
            if (googleApiClient == null) {
                baseContext.connectGoogleApi()
                        .subscribe({ googleApi ->
                            googleApiClient = googleApi
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    googleApi,
                                    LocationRequest()
                                            .setInterval(LOCATION_UPDATE_INTERVAL_MILLIS)
                                            .setFastestInterval(MIN_UPDATE_INTERVAL_MILLIS),
                                    this)
                        })
            }
        } else if (ACTION_STOP_LOCATION == action) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        locationBoard.locationStatus.onNext(LocationBoard.Status.IDLE)
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
        googleApiClient?.disconnect()
        super.onDestroy()
    }

    override fun onLocationChanged(location: Location?) {
        val routeId = Preferences.Route.id
        if (routeId == null) {
            w("Skipping location push - no route created")
            return
        }
        if (location == null) {
            return
        }
        i("Location ${location.elapsedRealtimeNanos}, ${location.longitude}, ${location.latitude}, at ${location.accuracy}m")
        comingApi.pushRoutePoint(routeId, Api.PushPointRequest(
                location.elapsedRealtimeNanos,
                location.latitude,
                location.longitude))
                .subscribeOn(Schedulers.io())
                .subscribe({ result -> },
                        { error -> e(error.message!!)})
    }

    override fun onCreate() {
        super.onCreate()

        val notification = NotificationCompat.Builder(this)
                //todo user mask here
                .setSmallIcon(R.drawable.man1)
                .setContentTitle("You are sharing your location now")
                .addAction(R.drawable.ic_stat_name, "stop", stopLocationPendingIntent(baseContext))
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.man1))
                .setStyle(NotificationCompat.BigTextStyle().bigText("You are sharing your location now"))
                .build()

        startForeground(13243546, notification)
    }

    override fun onBind(p0: Intent?): IBinder {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private val ACTION_START_LOCATION = "org.c8.research.comming.action.START_SERVICE"
        private val ACTION_STOP_LOCATION = "org.c8.research.comming.action.STOP_SERVICE"

        internal fun makeIntent(context: Context, action: String): Intent {
            val intent = Intent(context, LocationTrackingService::class.java)
            intent.action = action
            return intent
        }

        internal fun startLocationPendingIntent(context: Context): PendingIntent {
            return PendingIntent.getService(context, 0, makeIntent(context, ACTION_START_LOCATION), PendingIntent.FLAG_CANCEL_CURRENT)
        }

        internal fun stopLocationPendingIntent(context: Context): PendingIntent {
            return PendingIntent.getService(context, 0, makeIntent(context, ACTION_STOP_LOCATION), PendingIntent.FLAG_CANCEL_CURRENT)
        }
    }
}
