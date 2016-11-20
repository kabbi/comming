package org.c8.research.comming

import android.content.Context
import com.google.gson.GsonBuilder
import org.c8.research.comming.entities.Api
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import rx.Observable

interface CommingApi {

    @POST("/routes")
    fun createRoute(@Body request: Api.NewRouteRequest): Observable<Api.NewRouteResponse>

    @POST("/routes/{id}/settings")
    fun updateRouteSettings(@Path("id") id: String, @Body settings: Api.RouteSettings): Observable<Unit>

    @POST("/points/{routeId}")
    fun pushRoutePoint(@Path("routeId") routeId: String, @Body request: Api.PushPointRequest): Observable<Unit>

    companion object {
        fun create(context: Context) : CommingApi {
            val gsonBuilder = GsonBuilder()

            val restAdapter = Retrofit.Builder()
                    .baseUrl(context.getString(R.string.api_host))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .build()

            return restAdapter.create(CommingApi::class.java)
        }
    }
}