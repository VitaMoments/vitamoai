package eu.vitamo.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import eu.vitamo.app.infrastructure.app.AppInitializer
import eu.vitamo.app.infrastructure.di.initKoin
import org.koin.mp.KoinPlatform

fun MainViewController() = run {
	initKoin()

	val appInitializer =
		KoinPlatform
			.getKoin()
			.get<AppInitializer>()

	ComposeUIViewController {
		var initialized by remember {
			mutableStateOf(false)
		}

		LaunchedEffect(Unit) {
			appInitializer.initialize()
			initialized = true
		}

		if (initialized) {
			App()
		}
	}
}