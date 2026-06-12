package eu.vitamo.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform