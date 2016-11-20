package org.c8.research.comming.entities

object Api {
    data class RouteSettings(val avatar: String, val name: String)
    data class NewRouteRequest(val avatar: String, val name: String)
    data class NewRouteResponse(val _id: String, val url: String)
    data class PushPointRequest(
            //val accuracy: Float,
            //val altitude: Double,
            //val bearing: Float,
            val elapsedRealtimeNanos: Long,
            val latitude: Double,
            val longitude: Double)
            //val speed: Float)
}