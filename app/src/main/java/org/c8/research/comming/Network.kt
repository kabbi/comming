package org.c8.research.comming

import android.content.Context
import com.google.gson.GsonBuilder
import org.c8.research.comming.entities.Api
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import retrofit.RxJavaCallAdapterFactory
import retrofit.http.Body
import retrofit.http.POST
import retrofit.http.Path
import rx.Observable

interface CommingService {

    @POST("/routes")
    fun createRoute(@Body request: Api.NewRouteRequest): Observable<Api.NewRouteResponse>

    @POST("/routes/{id}/settings")
    fun updateRouteSettings(@Path("id") id: String, @Body settings: Api.RouteSettings): Observable<Unit>

    @POST("/points/{routeId}")
    fun pushRoutePoint(@Path("routeId") routeId: String, @Body request: Api.PushPointRequest): Observable<Unit>

    companion object {
        fun create(context: Context) : CommingService {
            val gsonBuilder = GsonBuilder()

            val restAdapter = Retrofit.Builder()
                    .baseUrl(context.getString(R.string.api_host))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .build()

            return restAdapter.create(CommingService::class.java)
        }
    }
}