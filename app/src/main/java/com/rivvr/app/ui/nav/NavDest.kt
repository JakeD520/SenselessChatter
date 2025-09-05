package com.rivvr.app.ui.nav

sealed interface NavDest {
    val route: String
    val label: String

    data object PrivateRoom : NavDest { override val route = "private_room"; override val label = "Private Room" }
    data object MainRoom : NavDest { override val route = "main_room"; override val label = "Main Room" }
    data object PrivateMessage : NavDest { override val route = "private_message"; override val label = "Private Message" }
    data object Dashboard : NavDest { override val route = "dashboard"; override val label = "Dashboard" }
}


