package eu.vitamo.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import eu.vitamo.app.di.initKoin
import eu.vitamo.app.network.auth.initializeAuthCookiePersistence

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initializeAuthCookiePersistence(applicationContext)
        initKoin()
        val initialDeepLink = intent?.dataString

        initialDeepLink?.let { link ->
            Log.d("MainActivity", "Initial deep link: $link")
        }

        setContent {
            App(
                initialDeepLink = initialDeepLink,
            )
        }
    }
}