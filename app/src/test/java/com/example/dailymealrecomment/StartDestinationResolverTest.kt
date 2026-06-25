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
            StartDestinationResolver.resolve(hasFirebaseUser = false, isProfileCompleted = false),
        )
    }

    @Test
    fun signedInUserWithoutProfileMustLoadProfile() {
        assertEquals(
            StartDestination.PROFILE_LOOKUP,
            StartDestinationResolver.resolve(hasFirebaseUser = true, isProfileCompleted = false),
        )
    }

    @Test
    fun signedInUserWithCompletedProfileGoesToMain() {
        assertEquals(
            StartDestination.MAIN,
            StartDestinationResolver.resolve(hasFirebaseUser = true, isProfileCompleted = true),
        )
    }
}
