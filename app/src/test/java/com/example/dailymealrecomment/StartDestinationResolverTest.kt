package com.example.dailymealrecomment

import com.example.dailymealrecomment.data.StartDestination
import com.example.dailymealrecomment.data.StartDestinationResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class StartDestinationResolverTest {
    @Test
    fun signedOutUserMustSeeLogin() {
        assertEquals(
            StartDestination.LOGIN,
            StartDestinationResolver.resolve(hasAuthenticatedSession = false, isProfileCompleted = false),
        )
    }

    @Test
    fun signedInUserWithoutProfileMustLoadProfile() {
        assertEquals(
            StartDestination.PROFILE_LOOKUP,
            StartDestinationResolver.resolve(hasAuthenticatedSession = true, isProfileCompleted = false),
        )
    }

    @Test
    fun signedInUserWithCompletedProfileGoesToMain() {
        assertEquals(
            StartDestination.MAIN,
            StartDestinationResolver.resolve(hasAuthenticatedSession = true, isProfileCompleted = true),
        )
    }
}
