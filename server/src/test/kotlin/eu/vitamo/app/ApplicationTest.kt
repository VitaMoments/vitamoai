package eu.vitamo.app

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlin.test.*

class ApplicationTest {

    @Serializable
    private data class SampleRequest(
        val name: String,
    )

    @Test
    fun testRoot() = testApplication {
        System.setProperty("VITAMO_SKIP_DB_INIT", "true")
        System.setProperty("JWT_ISSUER", "test-issuer")
        System.setProperty("JWT_AUDIENCE", "test-audience")
        System.setProperty("JWT_SECRET", "test-secret")
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, Ktor!", response.bodyAsText())
        System.clearProperty("VITAMO_SKIP_DB_INIT")
        System.clearProperty("JWT_ISSUER")
        System.clearProperty("JWT_AUDIENCE")
        System.clearProperty("JWT_SECRET")
    }

    @Test
    fun malformedJson_returnsBadRequest() = testApplication {
        System.setProperty("VITAMO_SKIP_DB_INIT", "true")
        System.setProperty("JWT_ISSUER", "test-issuer")
        System.setProperty("JWT_AUDIENCE", "test-audience")
        System.setProperty("JWT_SECRET", "test-secret")
        application {
            module()
            routing {
                post("/echo") {
                    call.receive<SampleRequest>()
                    call.respondText("ok")
                }
            }
        }

        val response = client.post("/echo") {
            contentType(ContentType.Application.Json)
            setBody("{\"name\":")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Invalid JSON", response.bodyAsText())
        System.clearProperty("VITAMO_SKIP_DB_INIT")
        System.clearProperty("JWT_ISSUER")
        System.clearProperty("JWT_AUDIENCE")
        System.clearProperty("JWT_SECRET")
    }
}
