package org.c8.research.comming.entities

object Api {
    data class RouteSettings(val avatar: String, val title: String)
    data class NewRouteRequest(val avatar: String, val title: String)
    data class NewRouteResponse(val id: String, val url: String)
    data class PushPointRequest(val longitude: Double, val latitude: Double, val captureTimeNanos: Long)
}