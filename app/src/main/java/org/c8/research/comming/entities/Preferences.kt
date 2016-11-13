package org.c8.research.comming.entities

import com.chibatching.kotpref.KotprefModel

object Preferences {
    object Settings : KotprefModel() {
        var title: String by stringPrefVar(default = "The Godzilla")
        var avatar: String by stringPrefVar(default = "ava1")

        fun toRouteSettings() = Api.RouteSettings(avatar, title)
    }
    object Route : KotprefModel() {
        var id: String? by stringNullablePrefVar()
        var url: String? by stringNullablePrefVar()
    }
}
