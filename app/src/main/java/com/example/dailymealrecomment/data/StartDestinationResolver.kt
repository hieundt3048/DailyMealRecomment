package com.example.dailymealrecomment.data

enum class StartDestination {
    LOGIN,
    PROFILE_LOOKUP,
    MAIN,
}

object StartDestinationResolver {
    fun resolve(hasFirebaseUser: Boolean, isProfileCompleted: Boolean): StartDestination {
        if (!hasFirebaseUser) return StartDestination.LOGIN
        return if (isProfileCompleted) StartDestination.MAIN else StartDestination.PROFILE_LOOKUP
    }
}
