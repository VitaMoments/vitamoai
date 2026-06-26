package eu.vitamo.app.database.helpers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

suspend fun <T> dbQuery(coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO, block: suspend () -> T): T =
    withContext(coroutineDispatcher) {
        suspendTransaction { block() }
    }

