package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.kotlinStringAsJs
import io.sentry.kotlin.multiplatform.external.newJsObject
import io.sentry.kotlin.multiplatform.protocol.SentryId
import io.sentry.kotlin.multiplatform.protocol.UserFeedback
import kotlin.js.JsAny

/**
 * Converts a [UserFeedback] into the @sentry/browser `SendFeedbackParams` JS object
 * that `Sentry.captureFeedback({...})` expects.
 *
 * - `source: "api"` is set so the entry surfaces in the new `/feedback/` dashboard
 *   (programmatic submissions without a source can be silently dropped on the
 *   server side).
 * - `associatedEventId` is OMITTED when `sentryId == SentryId.EMPTY_ID`. Setting
 *   the all-zeros id makes Sentry's processor route the feedback to the legacy
 *   "Issues → User Feedback" tab attached to a non-existent event instead of the
 *   new dashboard. Callers that have a real associated event can still pass it.
 */
internal fun UserFeedback.toBrowserFeedbackObject(): JsAny {
    val o = newJsObject()
    jsSetProperty(o, "source", kotlinStringAsJs("api"))
    if (sentryId != SentryId.EMPTY_ID) {
        jsSetProperty(o, "associatedEventId", kotlinStringAsJs(sentryId.toString()))
    }
    name?.let { jsSetProperty(o, "name", kotlinStringAsJs(it)) }
    email?.let { jsSetProperty(o, "email", kotlinStringAsJs(it)) }
    comments?.let { jsSetProperty(o, "message", kotlinStringAsJs(it)) }
    return o
}
