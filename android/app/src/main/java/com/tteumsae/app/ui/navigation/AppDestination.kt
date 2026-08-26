package com.tteumsae.app.ui.navigation

enum class AppDestination {
    HOME,
    SAVED,
    SETTINGS,
    LOCATION,
    CONDITIONS,
    LOADING,
    RESULTS,
    DETAIL,
}

enum class MainTab {
    EXPLORE,
    SAVED,
    SETTINGS,
}

fun previousDestination(current: AppDestination): AppDestination = when (current) {
    AppDestination.SAVED,
    AppDestination.SETTINGS,
    AppDestination.LOCATION,
    -> AppDestination.HOME

    AppDestination.CONDITIONS -> AppDestination.LOCATION
    AppDestination.LOADING,
    AppDestination.RESULTS,
    -> AppDestination.CONDITIONS

    AppDestination.DETAIL -> AppDestination.RESULTS
    AppDestination.HOME -> AppDestination.HOME
}
