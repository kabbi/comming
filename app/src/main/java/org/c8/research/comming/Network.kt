package org.c8.research.comming

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.c8.research.comming.entities.Api
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import rx.Observable


interface ComingApi {

    @POST("api/routes")
    fun createRoute(@Body request: Api.NewRouteRequest): Observable<Api.NewRouteResponse>

    @POST("api/routes/{id}/settings")
    fun updateRouteSettings(@Path("id") id: String, @Body settings: Api.RouteSettings): Observable<Unit>

    @POST("api/routes/{routeId}/points")
    fun pushRoutePoint(@Path("routeId") routeId: String, @Body request: Api.PushPointRequest): Observable<Unit>

    companion object {
        fun create(context: Context): ComingApi {
            val gsonBuilder = GsonBuilder()
            val okhttp = OkHttpClient.Builder()
                    .addNetworkInterceptor(StethoInterceptor())
                    .build()

            val restAdapter = Retrofit.Builder()
                    .baseUrl(context.getString(R.string.api_host))
                    .client(okhttp)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .build()

            return restAdapter.create(ComingApi::class.java)
        }
    }
}