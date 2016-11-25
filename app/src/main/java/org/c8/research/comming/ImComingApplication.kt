package org.c8.research.comming

import android.app.Application
import com.facebook.stetho.Stetho
import com.mapbox.mapboxsdk.MapboxAccountManager

class ImComingApplication : Application() {

    val locationBoard by lazy { LocationBoard(this) }
    val comingApi by lazy { ComingApi.create(this) }

    override fun onCreate() {
        super.onCreate()

        // Various third-party setups here
        MapboxAccountManager.start(this, getString(R.string.mapbox_api_key))
        Stetho.initializeWithDefaults(this)
    }
}