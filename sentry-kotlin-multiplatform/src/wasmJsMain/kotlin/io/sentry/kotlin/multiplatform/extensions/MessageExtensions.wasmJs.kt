package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.external.jsArrayGet
import io.sentry.kotlin.multiplatform.external.jsArrayLength
import io.sentry.kotlin.multiplatform.external.jsArrayPush
import io.sentry.kotlin.multiplatform.external.jsGetProperty
import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.jsTypeof
import io.sentry.kotlin.multiplatform.external.jsValueToString
import io.sentry.kotlin.multiplatform.external.jsArray
import io.sentry.kotlin.multiplatform.external.kotlinStringAsJs
import io.sentry.kotlin.multiplatform.external.newJsObject
import io.sentry.kotlin.multiplatform.protocol.Message
import kotlin.js.JsAny
import kotlin.js.unsafeCast

/** Mirrors Cocoa's Message mapping — browser protocol uses a plain object or formatted string. */
internal fun Message.toBrowserMessageObject(): JsAny {
    val scope = this
    val formattedFallback =
        scope.formatted?.takeIf { it.isNotEmpty() }
            ?: scope.message?.takeIf { it.isNotEmpty() }
            ?: ""

    val obj = newJsObject()
    jsSetProperty(obj, "formatted", kotlinStringAsJs(formattedFallback))
    scope.message?.let { jsSetProperty(obj, "message", kotlinStringAsJs(it)) }

    val paramsList = scope.params
    if (!paramsList.isNullOrEmpty()) {
        val arr = jsArray()
        paramsList.forEach { jsArrayPush(arr, kotlinStringAsJs(it)) }
        jsSetProperty(obj, "params", arr)
    }
    return obj
}

internal fun JsAny?.toKmpMessage(): Message {
    val root = this ?: return Message()

    // Browser events may use a plain string for `message` instead of `{ formatted, message, params }`.
    if (jsTypeof(root) == "string") {
        val s = jsValueToString(root)
        return Message(formatted = s.takeUnless { it.isEmpty() } ?: "")
    }

    val formatted =
        jsGetProperty(root, "formatted")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }

    val message =
        jsGetProperty(root, "message")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }

    val paramsJs = jsGetProperty(root, "params")
    val params =
        paramsJs?.let { params ->
            val asArray = params.unsafeCast<JsAny>()
            val len = jsArrayLength(asArray)
            if (len <= 0) {
                null
            } else {
                buildList {
                    repeat(len) { idx ->
                        jsArrayGet(asArray, idx)?.let {
                            add(jsValueToString(it))
                        }
                    }
                }.takeUnless { it.isEmpty() }
            }
        }

    val formattedAdjusted =
        if (message != null && formatted == message) {
            null
        } else {
            formatted
        }

    return Message(message = message, params = params, formatted = formattedAdjusted)
}
