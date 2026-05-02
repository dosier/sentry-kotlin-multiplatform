package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.external.jsArray
import io.sentry.kotlin.multiplatform.external.jsArrayGet
import io.sentry.kotlin.multiplatform.external.jsArrayLength
import io.sentry.kotlin.multiplatform.external.jsArrayPush
import io.sentry.kotlin.multiplatform.external.jsGetProperty
import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.jsValueToString
import io.sentry.kotlin.multiplatform.external.kotlinDoubleAsJs
import io.sentry.kotlin.multiplatform.external.kotlinStringAsJs
import io.sentry.kotlin.multiplatform.external.newJsObject
import io.sentry.kotlin.multiplatform.protocol.SentryException
import kotlin.js.JsAny
import kotlin.js.unsafeCast

internal fun SentryException.toBrowserExceptionValue(): JsAny {
    val obj = newJsObject()
    type?.let { jsSetProperty(obj, "type", kotlinStringAsJs(it)) }
    value?.let { jsSetProperty(obj, "value", kotlinStringAsJs(it)) }
    module?.let { jsSetProperty(obj, "module", kotlinStringAsJs(it)) }
    threadId?.let { jsSetProperty(obj, "thread_id", kotlinDoubleAsJs(it.toDouble())) }
    return obj
}

internal fun JsAny.toKmpSentryException(): SentryException {
    val threadRaw = jsGetProperty(this, "thread_id")
    val threadDouble =
        threadRaw?.let {
            jsValueToDoubleOrNull(it)
        }
    val threadLong = threadDouble?.toLong()

    val type =
        jsGetProperty(this, "type")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }
    val value =
        jsGetProperty(this, "value")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }
    val module =
        jsGetProperty(this, "module")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }

    return SentryException(
        type = type,
        value = value,
        module = module,
        threadId = threadLong,
    )
}

@Suppress("UNUSED_PARAMETER")
private fun jsValueToDoubleOrNull(value: JsAny?): Double? =
    js("(typeof value === 'number' && !isNaN(value) ? value : null)")

internal fun kmpExceptionsToBrowserExceptionEnvelope(values: List<SentryException>): JsAny {
    val arr = jsArray()
    values.forEach { jsArrayPush(arr, it.toBrowserExceptionValue()) }
    val envelope = newJsObject()
    jsSetProperty(envelope, "values", arr)
    return envelope
}

internal fun browserExceptionEnvelopeToKmp(valuesJs: JsAny?): MutableList<SentryException> {
    if (valuesJs == null) return mutableListOf()
    val arr = valuesJs.unsafeCast<JsAny>()
    val len = jsArrayLength(arr)
    val out = mutableListOf<SentryException>()
    var i = 0
    while (i < len) {
        jsArrayGet(arr, i)?.let { slot ->
            out.add(slot.toKmpSentryException())
        }
        i++
    }
    return out
}
