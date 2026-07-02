package eu.vitamo.app.features.feed.routes

import eu.vitamo.app.features.feed.validation.FeedInputValidator
import eu.vitamo.app.features.feed.sampleCreateRequest
import kotlin.test.Test
import kotlin.test.assertFails

class FeedCreateRouteTest {
    @Test
    fun `rejects empty content payload`() {
        val validator = FeedInputValidator()
        assertFails {
            validator.validateCreate(sampleCreateRequest(text = ""))
        }
    }
}
