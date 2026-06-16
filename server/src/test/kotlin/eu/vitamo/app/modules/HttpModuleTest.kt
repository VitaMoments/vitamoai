package eu.vitamo.app.modules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HttpModuleTest {

    @Test
    fun frontendHostFromEnvironment_returnsNullWhenMissingOrBlank() {
        assertNull(frontendHostFromEnvironment(emptyMap()))
        assertNull(frontendHostFromEnvironment(mapOf("FRONTEND_HOST" to "   ")))
    }

    @Test
    fun normalizeFrontendHost_acceptsHostAndFullUrlForms() {
        assertEquals("app.vitamo.nl", normalizeFrontendHost("app.vitamo.nl"))
        assertEquals("app.vitamo.nl:8443", normalizeFrontendHost("app.vitamo.nl:8443"))
        assertEquals("app.vitamo.nl", normalizeFrontendHost("https://app.vitamo.nl"))
        assertEquals("app.vitamo.nl:8443", normalizeFrontendHost("https://app.vitamo.nl:8443"))
    }

    @Test
    fun normalizeFrontendHost_rejectsValuesWithPathOrWhitespace() {
        assertNull(normalizeFrontendHost("https://app.vitamo.nl/path"))
        assertNull(normalizeFrontendHost("ftp://app.vitamo.nl"))
        assertNull(normalizeFrontendHost("app vitamo nl"))
    }
}

