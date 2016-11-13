package org.c8.research.comming

import android.app.Application
import com.mapbox.mapboxsdk.MapboxAccountManager

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        // Various third-party setups here
        MapboxAccountManager.start(this, getString(R.string.mapbox_api_key))
    }
}