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
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, Ktor!", response.bodyAsText())
    }

    @Test
    fun malformedJson_returnsBadRequest() = testApplication {
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
    }
}