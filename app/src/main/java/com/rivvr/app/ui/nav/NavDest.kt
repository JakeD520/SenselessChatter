package com.rivvr.app.ui.nav

sealed interface NavDest {
    val route: String
    val label: String

    data object Flows : NavDest { override val route = "flows"; override val label = "Flows" }
    data object Feed : NavDest  { override val route = "feed";  override val label = "Feed" }
    data object Profile : NavDest { override val route = "profile"; override val label = "Profile" }
}


