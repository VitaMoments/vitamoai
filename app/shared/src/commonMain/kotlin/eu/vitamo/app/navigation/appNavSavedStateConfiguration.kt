package eu.vitamo.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val appNavSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
//            Authenticated destinations
            subclass(MainDestination.Home::class, MainDestination.Home.serializer())
            subclass(MainDestination.FeedList::class, MainDestination.FeedList.serializer())
            subclass(MainDestination.FeedCreate::class, MainDestination.FeedCreate.serializer())
            subclass(MainDestination.FeedDetail::class, MainDestination.FeedDetail.serializer())

//            Authentication Destinations
            subclass(AuthDestination.Login::class, AuthDestination.Login.serializer())
            subclass(AuthDestination.Register::class, AuthDestination.Register.serializer())
            subclass(AuthDestination.VerifyEmailAddress::class, AuthDestination.VerifyEmailAddress.serializer())
            subclass(AuthDestination.ForgotPassword::class, AuthDestination.ForgotPassword.serializer())
            subclass(AuthDestination.ResetPassword::class, AuthDestination.ResetPassword.serializer())
        }
    }
}