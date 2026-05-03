package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.CocoaSentryFeedback
import io.sentry.kotlin.multiplatform.CocoaSentryId
import io.sentry.kotlin.multiplatform.protocol.SentryId
import io.sentry.kotlin.multiplatform.protocol.UserFeedback

/**
 * Converts a [UserFeedback] into a sentry-cocoa [CocoaSentryFeedback] for the new
 * User Feedback v2 API (`SentrySDK.captureFeedback`).
 *
 * - source = 1 (`SentryFeedbackSourceCustom`) so the entry surfaces in the new
 *   `/feedback/` dashboard instead of the legacy "Issues → User Feedback" tab.
 * - `associatedEventId` is omitted (null) when [UserFeedback.sentryId] is
 *   [SentryId.EMPTY_ID], to avoid attaching the entry to a non-existent event.
 */
internal fun UserFeedback.toCocoaFeedback(): CocoaSentryFeedback {
    val message = comments ?: "(no comment)"
    val associated: CocoaSentryId? =
        if (sentryId != SentryId.EMPTY_ID) CocoaSentryId(sentryId.toString()) else null
    // SentryFeedbackSource is an Int (NSInteger) typedef; SentryFeedbackSourceCustom == 1 (programmatic/api)
    return CocoaSentryFeedback(
        message = message,
        name = name,
        email = email,
        source = 1,
        associatedEventId = associated,
        attachments = null
    )
}
