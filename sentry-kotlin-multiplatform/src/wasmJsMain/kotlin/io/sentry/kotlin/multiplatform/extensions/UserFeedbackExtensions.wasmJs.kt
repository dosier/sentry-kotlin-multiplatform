package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.kotlinStringAsJs
import io.sentry.kotlin.multiplatform.external.newJsObject
import io.sentry.kotlin.multiplatform.protocol.UserFeedback
import kotlin.js.JsAny

internal fun UserFeedback.toBrowserFeedbackObject(): JsAny {
    val o = newJsObject()
    jsSetProperty(o, "associatedEventId", kotlinStringAsJs(sentryId.toString()))
    name?.let { jsSetProperty(o, "name", kotlinStringAsJs(it)) }
    email?.let { jsSetProperty(o, "email", kotlinStringAsJs(it)) }
    comments?.let { jsSetProperty(o, "message", kotlinStringAsJs(it)) }
    return o
}
