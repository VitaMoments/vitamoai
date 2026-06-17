plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
}

group = "eu.vitamo.app"
version = "1.0.0"
application {
    mainClass = "eu.vitamo.app.ApplicationKt"
}

dependencies {
    api(projects.core)
    implementation(libs.koin.core)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.auto.head.response.jvm)
    implementation(libs.ktor.server.compression.jvm)
    implementation(libs.ktor.server.conditional.headers.jvm)
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.default.headers.jvm)
    implementation(libs.ktor.server.forwarded.header.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.server.status.pages.jvm)
    implementation(libs.ktor.server.auth.jvm)
    implementation(libs.ktor.server.auth.jwt.jvm)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    implementation(libs.postgresql)
    implementation(libs.hikari)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
