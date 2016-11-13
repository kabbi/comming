package org.c8.research.comming.utils

import android.content.Context
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import rx.Observable

fun Context.connectGoogleApi(): Observable<GoogleApiClient> = Observable.create { subscriber ->
    var client: GoogleApiClient? = null

    val clientBuilder = GoogleApiClient.Builder(this)
    clientBuilder.addApi(LocationServices.API)
    clientBuilder.addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnectionSuspended(code: Int) = Unit
        override fun onConnected(bundle: Bundle?) {
            // TODO: Pass bundle here somehow
            subscriber.onNext(client)
        }
    })
    clientBuilder.addOnConnectionFailedListener {
        // TODO: Make custom error and pass connection result here
        subscriber.onError(Error())
    }

    client = clientBuilder.build()
    client.connect()
}

val Context.localBroadcastManager: LocalBroadcastManager
    get() = LocalBroadcastManager.getInstance(this)
