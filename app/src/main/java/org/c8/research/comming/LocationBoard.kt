package org.c8.research.comming

import android.content.Context
import com.pawegio.kandroid.e
import org.c8.research.comming.entities.Api
import org.c8.research.comming.entities.Preferences
import org.c8.research.comming.services.LocationTrackingService
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

/**
 * Created by crocodilys on 11/20/16.
 * email: crocodilys@yandex-team.ru
 */
class LocationBoard(applicationContext: Context) {

    val comingApi by lazy { (applicationContext as ImComingApplication).comingApi }
    val locationStatus = BehaviorSubject.create<Status>(Status.IDLE)!!

    enum class Status {
        IDLE, PREPARING, RUNNING, ERROR
    }

    fun startLocationService(context: Context) {
        locationStatus.onNext(Status.PREPARING)
        comingApi.createRoute(Api.NewRouteRequest("av1", "The Godzilla"))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ route ->
                        Preferences.Route.id = route._id
                        Preferences.Route.url = route.url
                        LocationTrackingService.startLocationPendingIntent(context).send()
                        locationStatus.onNext(Status.RUNNING)
                    }, { error ->
                        e(error.message!!)
                        locationStatus.onNext(Status.ERROR)
                    })
    }

    fun stopLocationService(context: Context) {
        locationStatus.onNext(Status.IDLE)
        LocationTrackingService.stopLocationPendingIntent(context).send()
    }
}