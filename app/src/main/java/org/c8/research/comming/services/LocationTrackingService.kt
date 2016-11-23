package org.c8.research.comming.services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
import com.pawegio.kandroid.wtf
import org.c8.research.comming.CommingApi
import org.c8.research.comming.R
import org.c8.research.comming.entities.Api
import org.c8.research.comming.entities.Preferences
import org.c8.research.comming.utils.connectGoogleApi


const val LOCATION_UPDATE_INTERVAL_MILLIS: Long = 1000
const val MIN_UPDATE_INTERVAL_MILLIS: Long = 1000

class LocationTrackingService : Service() {

    val commingService by lazy {
        CommingApi.create(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val action = intent!!.action
        if (ACTION_PUSH_LOCATION == action) {
            if (LocationResult.hasResult(intent)) {
                handleLocationInfo(LocationResult.extractResult(intent))
            } else if (LocationAvailability.hasLocationAvailability(intent)) {
                handleAvailabilityChanged(LocationAvailability.extractLocationAvailability(intent))
            } else {
                wtf("Intent from gsm does not have valid data")
            }
        } else if (ACTION_STOP_LOCATION == action) {
            baseContext.connectGoogleApi()
                    .subscribe({ googleApi ->
                        LocationTrackingService.stopLocationTracking(baseContext, googleApi)
                        stopSelf()
                    })
        }
        return START_NOT_STICKY
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

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun handleAvailabilityChanged(availability: LocationAvailability) {
        // TODO: Inform the server maybe
        i("Availability ${availability.isLocationAvailable}")
    }

    private fun handleLocationInfo(result: LocationResult) {
        val routeId = Preferences.Route.id
        if (routeId == null) {
            w("Skipping location push - no route created")
            return
        }
        result.locations.forEach {
            i("Location ${it.elapsedRealtimeNanos}, ${it.longitude}, ${it.latitude}, at ${it.accuracy}m")
            commingService.pushRoutePoint(
                    routeId,
                    Api.PushPointRequest(
                            it.elapsedRealtimeNanos,
                            it.latitude,
                            it.longitude)
            ).subscribe({result -> }, {error -> })
        }
    }

    override fun onBind(p0: Intent?): IBinder {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private val ACTION_PUSH_LOCATION = "org.c8.research.comming.action.PUSH_LOCATION"
        private val ACTION_STOP_LOCATION = "org.c8.research.comming.action.STOP_LOCATION"

        internal fun makePushLocationIntent(context: Context): Intent {
            val intent = Intent(context, LocationTrackingService::class.java)
            intent.action = ACTION_PUSH_LOCATION
            return intent
        }

        internal fun makeStopLocationIntent(context: Context): Intent {
            val intent = Intent(context, LocationTrackingService::class.java)
            intent.action = ACTION_STOP_LOCATION
            return intent
        }

        internal fun createPushLocationPendingIntent(context: Context): PendingIntent {
            return PendingIntent.getService(context, 0, makePushLocationIntent(context), PendingIntent.FLAG_CANCEL_CURRENT)
        }

        internal fun getPushLocationPendingIntent(context: Context): PendingIntent? {
            return PendingIntent.getService(context, 0, makePushLocationIntent(context), PendingIntent.FLAG_NO_CREATE)
        }

        internal fun stopLocationPendingIntent(context: Context): PendingIntent {
            return PendingIntent.getService(context, 0, makeStopLocationIntent(context), PendingIntent.FLAG_CANCEL_CURRENT)
        }

        fun startLocationTracking(context: Context, googleApi: GoogleApiClient) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApi,
                    LocationRequest()
                            .setInterval(LOCATION_UPDATE_INTERVAL_MILLIS)
                            .setFastestInterval(MIN_UPDATE_INTERVAL_MILLIS),
                    createPushLocationPendingIntent(context))
        }

        fun stopLocationTracking(context: Context, googleApi: GoogleApiClient) {
            val intent = getPushLocationPendingIntent(context) ?: return
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApi, intent)
        }
    }
}
