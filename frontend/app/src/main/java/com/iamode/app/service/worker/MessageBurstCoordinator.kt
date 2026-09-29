package com.iamode.app.service.worker

import com.iamode.app.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debounces rapid notification updates from one conversation. The latest message resets the
 * quiet-period timer, so the AI receives one coherent thought instead of replying mid-burst.
 */
@Singleton
class MessageBurstCoordinator @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val jobs = ConcurrentHashMap<String, Job>()

    fun schedule(conversationId: String, afterQuietMillis: Long = QUIET_PERIOD_MS, action: suspend () -> Unit) {
        jobs.remove(conversationId)?.cancel()
        jobs[conversationId] = scope.launch {
            delay(afterQuietMillis)
            coroutineContext[Job]?.let { jobs.remove(conversationId, it) }
            action()
        }
    }

    fun cancel(conversationId: String) { jobs.remove(conversationId)?.cancel() }

    companion object { const val QUIET_PERIOD_MS = 12_000L }
}
