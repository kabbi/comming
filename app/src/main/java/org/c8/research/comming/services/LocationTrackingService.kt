package org.c8.research.comming.services

import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
import com.pawegio.kandroid.wtf
import org.c8.research.comming.CommingApi
import org.c8.research.comming.entities.Api
import org.c8.research.comming.entities.Preferences

const val LOCATION_UPDATE_INTERVAL_MILLIS: Long = 1000
const val MIN_UPDATE_INTERVAL_MILLIS: Long = 1000

class LocationTrackingService : IntentService("LocationTrackingService") {
    val commingService by lazy {
        CommingApi.create(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            return
        }

        val action = intent.action
        if (ACTION_PUSH_LOCATION == action) {
            if (LocationResult.hasResult(intent)) {
                handleLocationInfo(LocationResult.extractResult(intent))
            } else if (LocationAvailability.hasLocationAvailability(intent)) {
                handleAvailabilityChanged(LocationAvailability.extractLocationAvailability(intent))
            } else {
                wtf("Intent from gsm does not have valid data")
            }
        }
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
            commingService.pushRoutePoint(routeId, Api.PushPointRequest(it.longitude, it.latitude, it.elapsedRealtimeNanos))
        }
    }

    companion object {
        private val ACTION_PUSH_LOCATION = "org.c8.research.comming.action.PUSH_LOCATION"

        internal fun makePushLocationIntent(context: Context): Intent {
            val intent = Intent(context, LocationTrackingService::class.java)
            intent.action = ACTION_PUSH_LOCATION
            return intent
        }

        internal fun createPushLocationPendingIntent(context: Context): PendingIntent {
            return PendingIntent.getService(context, 0, makePushLocationIntent(context), PendingIntent.FLAG_CANCEL_CURRENT)
        }

        internal fun getPushLocationPendingIntent(context: Context): PendingIntent? {
            return PendingIntent.getService(context, 0, makePushLocationIntent(context), PendingIntent.FLAG_NO_CREATE)
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

        fun isTrackingLocation(context: Context): Boolean {
            return getPushLocationPendingIntent(context) != null
        }
    }
}
