package eu.vitamo.app.infrastructure.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val appNavSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
//            ErrorDestinations
            subclass(ErrorDestination.Unavailable::class, ErrorDestination.Unavailable.serializer())

//            Authenticated destinations
            subclass(MainDestination.Home::class, MainDestination.Home.serializer())
            subclass(MainDestination.Profile::class, MainDestination.Profile.serializer())
            subclass(MainDestination.Settings::class, MainDestination.Settings.serializer())
            subclass(MainDestination.Users::class, MainDestination.Users.serializer())
            subclass(MainDestination.FriendRequests::class, MainDestination.FriendRequests.serializer())

            subclass(FeedDestination.CreatePost::class, FeedDestination.CreatePost.serializer())

//            Authentication Destinations
            subclass(AuthDestination.Login::class, AuthDestination.Login.serializer())
            subclass(AuthDestination.Register::class, AuthDestination.Register.serializer())
            subclass(AuthDestination.VerifyEmailAddress::class, AuthDestination.VerifyEmailAddress.serializer())
            subclass(AuthDestination.ForgotPassword::class, AuthDestination.ForgotPassword.serializer())
            subclass(AuthDestination.ResetPassword::class, AuthDestination.ResetPassword.serializer())
        }
    }
}