package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.JvmFeedback
import io.sentry.kotlin.multiplatform.JvmSentryId
import io.sentry.kotlin.multiplatform.protocol.SentryId
import io.sentry.kotlin.multiplatform.protocol.UserFeedback

/**
 * Converts a [UserFeedback] into a sentry-java [JvmFeedback] for the new
 * User Feedback v2 API (`Sentry.captureFeedback`).
 *
 * - `source` is set to "api" automatically by sentry-java when `Sentry.captureFeedback`
 *   is called; we don't need to set it explicitly here.
 * - `associatedEventId` is omitted when [UserFeedback.sentryId] is [SentryId.EMPTY_ID],
 *   to keep the entry standalone in the `/feedback/` dashboard rather than attached
 *   to a non-existent event.
 */
internal fun UserFeedback.toJvmFeedback(): JvmFeedback {
    val message = comments ?: "(no comment)"
    return JvmFeedback(message).apply {
        this@toJvmFeedback.name?.let { setName(it) }
        this@toJvmFeedback.email?.let { setContactEmail(it) }
        if (this@toJvmFeedback.sentryId != SentryId.EMPTY_ID) {
            setAssociatedEventId(JvmSentryId(this@toJvmFeedback.sentryId.toString()))
        }
    }
}
