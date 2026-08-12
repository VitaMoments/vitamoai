package eu.vitamo.app.features.device.provider

import eu.vitamo.app.api.contracts.device.ClientPlatform
import eu.vitamo.app.api.contracts.device.ClientType
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

actual class PlatformClientInfoProvider actual constructor() {

    actual fun get(): PlatformClientInfo {
        val device = UIDevice.currentDevice

        val clientVersion = NSBundle.mainBundle
            .objectForInfoDictionaryKey(
                "CFBundleShortVersionString",
            )
            ?.toString()

        return PlatformClientInfo(
            clientType = ClientType.APP,
            clientName = "VitaMo",
            clientVersion = clientVersion,
            platform = ClientPlatform.IOS,
            osVersion = device.systemVersion,
            deviceName = device.name,
            deviceModel = device.model,
        )
    }
}