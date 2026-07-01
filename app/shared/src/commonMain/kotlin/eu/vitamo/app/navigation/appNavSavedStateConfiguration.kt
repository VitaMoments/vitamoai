package eu.vitamo.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val appNavSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainDestination.Home::class, MainDestination.Home.serializer())

            subclass(AuthDestination.Login::class, AuthDestination.Login.serializer())
            subclass(AuthDestination.Register::class, AuthDestination.Register.serializer())
            subclass(AuthDestination.VerifyEmailAddress::class, AuthDestination.VerifyEmailAddress.serializer())
            subclass(AuthDestination.ForgotPassword::class, AuthDestination.ForgotPassword.serializer())
            subclass(AuthDestination.ResetPassword::class, AuthDestination.ResetPassword.serializer())
        }
    }
}