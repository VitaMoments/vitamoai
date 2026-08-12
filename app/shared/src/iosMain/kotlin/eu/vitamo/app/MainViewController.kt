package eu.vitamo.app

import androidx.compose.ui.window.ComposeUIViewController
import eu.vitamo.app.infrastructure.di.initKoin

fun MainViewController() = ComposeUIViewController {
	initKoin()
	App()
}
