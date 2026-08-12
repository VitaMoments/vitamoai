package eu.vitamo.app.features.device.provider

import android.os.Build
import eu.vitamo.app.api.contracts.device.ClientPlatform
import eu.vitamo.app.api.contracts.device.ClientType

actual class PlatformClientInfoProvider actual constructor() {

    actual fun get(): PlatformClientInfo {
        val manufacturer = Build.MANUFACTURER
            .orEmpty()
            .trim()

        val model = Build.MODEL
            .orEmpty()
            .trim()

        val deviceName = if (
            model.startsWith(
                prefix = manufacturer,
                ignoreCase = true,
            )
        ) {
            model
        } else {
            "$manufacturer $model".trim()
        }

        return PlatformClientInfo(
            clientType = ClientType.APP,
            clientName = "VitaMo",
            clientVersion = "0.0.1-SNAPSHOT",
            platform = ClientPlatform.ANDROID,
            osVersion = Build.VERSION.RELEASE,
            deviceName = deviceName,
            deviceModel = model,
        )
    }
}