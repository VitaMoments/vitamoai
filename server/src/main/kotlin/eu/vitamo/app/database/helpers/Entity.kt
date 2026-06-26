package eu.vitamo.app.database.helpers

import org.jetbrains.exposed.v1.dao.Entity
import kotlin.uuid.Uuid
val Entity<Uuid>.kotlinUuid: Uuid get() = this.id.value