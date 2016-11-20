package org.c8.research.comming

import android.content.Context
import org.c8.research.comming.entities.Api
import org.c8.research.comming.entities.Preferences
import org.c8.research.comming.services.LocationTrackingService
import org.c8.research.comming.utils.connectGoogleApi
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

/**
 * Created by crocodilys on 11/20/16.
 * email: crocodilys@yandex-team.ru
 */
class LocationBoard(applicationContext: Context) {

    val commingApi = CommingApi.create(applicationContext)
    val locationStatus = BehaviorSubject.create<Status>(Status.IDLE)

    enum class Status {
        IDLE, PREPARING, RUNNING, ERROR
    }

    fun startLocationService(context: Context) {
        locationStatus.onNext(Status.PREPARING)
        Observable.combineLatest(
                commingApi.createRoute(Api.NewRouteRequest("av1", "The Godzilla")),
                    context.connectGoogleApi(),
                    { a, b -> Pair(a, b)}
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        val (route, googleApi) = result
                        Preferences.Route.id = route._id
                        Preferences.Route.url = route.url
                        LocationTrackingService.startLocationTracking(context, googleApi)
                        googleApi.disconnect()
                        locationStatus.onNext(Status.RUNNING)
                    }, { error ->
                        locationStatus.onNext(Status.ERROR)
                    })
    }

    fun stopLocationService(context: Context) {
        context.connectGoogleApi()
                .subscribe({ googleApi ->
                    LocationTrackingService.stopLocationTracking(context, googleApi)
                    locationStatus.onNext(Status.IDLE)
                })
    }

    fun locationStatus(): Observable<Status> {
        return locationStatus
    }
}