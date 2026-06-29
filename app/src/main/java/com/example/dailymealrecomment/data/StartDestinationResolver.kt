package com.example.dailymealrecomment.data

enum class StartDestination {
    LOGIN,
    PROFILE_LOOKUP,
    MAIN,
}

object StartDestinationResolver {
    fun resolve(hasAuthenticatedSession: Boolean, isProfileCompleted: Boolean): StartDestination {
        if (!hasAuthenticatedSession) return StartDestination.LOGIN
        return if (isProfileCompleted) StartDestination.MAIN else StartDestination.PROFILE_LOOKUP
    }
}
