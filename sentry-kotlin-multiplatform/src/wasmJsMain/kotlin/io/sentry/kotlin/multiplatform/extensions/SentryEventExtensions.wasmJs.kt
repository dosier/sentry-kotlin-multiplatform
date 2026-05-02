package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.SentryEvent
import io.sentry.kotlin.multiplatform.external.BrowserBreadcrumb
import io.sentry.kotlin.multiplatform.external.BrowserUser
import io.sentry.kotlin.multiplatform.external.jsArray
import io.sentry.kotlin.multiplatform.external.jsArrayGet
import io.sentry.kotlin.multiplatform.external.jsArrayLength
import io.sentry.kotlin.multiplatform.external.jsArrayPush
import io.sentry.kotlin.multiplatform.external.jsGetProperty
import io.sentry.kotlin.multiplatform.external.jsIsArray
import io.sentry.kotlin.multiplatform.external.jsOwnStringKeys
import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.jsTypeof
import io.sentry.kotlin.multiplatform.external.jsValueToBooleanStrict
import io.sentry.kotlin.multiplatform.external.jsValueToDoubleStrict
import io.sentry.kotlin.multiplatform.external.jsValueToString
import io.sentry.kotlin.multiplatform.external.kotlinStringAsJs
import io.sentry.kotlin.multiplatform.external.newJsObject
import io.sentry.kotlin.multiplatform.external.toJsPropertyValue
import io.sentry.kotlin.multiplatform.protocol.SentryId
import kotlin.js.JsAny
import kotlin.js.unsafeCast

/**
 * Applies fields the KMP layer can set on an event onto a mutable @sentry/browser Event object
 * (e.g. the `event` argument in `beforeSend`).
 */
internal fun SentryEvent.applyToBrowserEvent(jsEvent: JsAny) {
    release?.let { jsSetProperty(jsEvent, "release", kotlinStringAsJs(it)) }
        ?: jsSetProperty(jsEvent, "release", null)
    dist?.let { jsSetProperty(jsEvent, "dist", kotlinStringAsJs(it)) }
        ?: jsSetProperty(jsEvent, "dist", null)
    environment?.let { jsSetProperty(jsEvent, "environment", kotlinStringAsJs(it)) }
    platform?.let { jsSetProperty(jsEvent, "platform", kotlinStringAsJs(it)) }
    serverName?.let { jsSetProperty(jsEvent, "server_name", kotlinStringAsJs(it)) }

    user?.let { u ->
        jsSetProperty(jsEvent, "user", u.toBrowserUser().unsafeCast())
    }

    jsSetProperty(jsEvent, "event_id", kotlinStringAsJs(eventId.toString()))

    level?.let { l ->
        jsSetProperty(jsEvent, "level", kotlinStringAsJs(l.toBrowserSeverityLevel()))
    }

    message?.let { m ->
        jsSetProperty(jsEvent, "message", m.toBrowserMessageObject())
    }

    logger?.let { jsSetProperty(jsEvent, "logger", kotlinStringAsJs(it)) }

    if (fingerprint.isNotEmpty()) {
        val fp = jsArray()
        fingerprint.forEach { jsArrayPush(fp, kotlinStringAsJs(it)) }
        jsSetProperty(jsEvent, "fingerprint", fp)
    }

    if (exceptions.isNotEmpty()) {
        jsSetProperty(
            jsEvent,
            "exception",
            kmpExceptionsToBrowserExceptionEnvelope(exceptions),
        )
    }

    if (breadcrumbs.isNotEmpty()) {
        val bcArr = jsArray()
        breadcrumbs.forEach { jsArrayPush(bcArr, it.toBrowserBreadcrumb().unsafeCast()) }
        jsSetProperty(jsEvent, "breadcrumbs", bcArr)
    }

    if (tags.isNotEmpty()) {
        val tagObj = newJsObject()
        tags.forEach { (key, value) ->
            jsSetProperty(tagObj, key, kotlinStringAsJs(value))
        }
        jsSetProperty(jsEvent, "tags", tagObj)
    }

    if (contexts.isNotEmpty()) {
        val ctxRoot = newJsObject()
        contexts.forEach { (name, payload) ->
            jsSetProperty(ctxRoot, name, payload.contextPayloadToJs())
        }
        jsSetProperty(jsEvent, "contexts", ctxRoot)
    }
}

internal fun browserEventSnapshotToKmp(jsEvent: JsAny): SentryEvent {
    val kmp = SentryEvent()

    jsGetProperty(jsEvent, "event_id")?.let {
        val idStr = jsValueToString(it).takeUnless { s -> s.isEmpty() }
        if (idStr != null) {
            kmp.eventId = SentryId(idStr)
        }
    }

    kmp.release =
        jsGetProperty(jsEvent, "release")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }
    kmp.dist =
        jsGetProperty(jsEvent, "dist")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }
    kmp.environment =
        jsGetProperty(jsEvent, "environment")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }
    kmp.platform =
        jsGetProperty(jsEvent, "platform")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }
    kmp.serverName =
        jsGetProperty(jsEvent, "server_name")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }

    jsGetProperty(jsEvent, "user")?.let {
        kmp.user = it.unsafeCast<BrowserUser>().toKmpUser()
    }

    jsGetProperty(jsEvent, "level")?.let {
        kmp.level = jsValueToString(it).toKmpSentryLevel()
    }

    jsGetProperty(jsEvent, "message")?.let {
        kmp.message = it.toKmpMessage()
    }

    kmp.logger =
        jsGetProperty(jsEvent, "logger")?.let {
            jsValueToString(it).takeUnless { s -> s.isEmpty() }
        }

    jsGetProperty(jsEvent, "fingerprint")?.let { fp ->
        val arr = fp.unsafeCast<JsAny>()
        val len = jsArrayLength(arr)
        var i = 0
        while (i < len) {
            jsArrayGet(arr, i)?.let {
                kmp.fingerprint.add(jsValueToString(it))
            }
            i++
        }
    }

    jsGetProperty(jsEvent, "exception")?.let { ex ->
        val values = jsGetProperty(ex, "values")
        kmp.exceptions = browserExceptionEnvelopeToKmp(values)
    }

    jsGetProperty(jsEvent, "breadcrumbs")?.let { list ->
        val arr = list.unsafeCast<JsAny>()
        val len = jsArrayLength(arr)
        var i = 0
        while (i < len) {
            jsArrayGet(arr, i)?.let {
                kmp.breadcrumbs.add(it.unsafeCast<BrowserBreadcrumb>().toKmpBreadcrumb())
            }
            i++
        }
    }

    jsGetProperty(jsEvent, "tags")?.let { tagObj ->
        val keys = jsOwnStringKeys(tagObj)
        val keyLen = jsArrayLength(keys)
        var ti = 0
        while (ti < keyLen) {
            val keyJs = jsArrayGet(keys, ti)
            ti++
            val key =
                keyJs?.let { jsValueToString(it) }?.takeUnless { s -> s.isEmpty() }
                    ?: continue
            jsGetProperty(tagObj, key)?.let { v ->
                kmp.setTag(key, jsValueToString(v))
            }
        }
    }

    jsGetProperty(jsEvent, "contexts")?.let {
        kmp.contexts = browserContextsToKmpMap(it.unsafeCast())
    }

    return kmp
}

private fun browserContextsToKmpMap(ctxRoot: JsAny): Map<String, Any> {
    val keys = jsOwnStringKeys(ctxRoot)
    val keyLen = jsArrayLength(keys)
    return buildMap {
        var i = 0
        while (i < keyLen) {
            val key =
                jsArrayGet(keys, i)?.let { jsValueToString(it) }?.takeUnless { s -> s.isEmpty() }
                    ?: run {
                        i++
                        continue
                    }
            i++
            val raw = jsGetProperty(ctxRoot, key) ?: continue
            put(key, jsContextValueToKotlin(raw) as Any)
        }
    }
}

private fun jsContextValueToKotlin(value: JsAny?): Any? {
    if (value == null || jsTypeof(value) == "undefined") {
        return null
    }
    return when (jsTypeof(value)) {
        "string" -> jsValueToString(value)
        "boolean" -> jsValueToBooleanStrict(value)
        "number" -> normalizeJsNumber(jsValueToDoubleStrict(value))
        "object" ->
            if (jsIsArray(value)) {
                jsArrayToContextList(value.unsafeCast())
            } else {
                browserContextsToKmpMap(value.unsafeCast())
            }

        else -> jsValueToString(value)
    }
}

private fun jsArrayToContextList(arr: JsAny): List<Any?> {
    val len = jsArrayLength(arr)
    return buildList(len) {
        repeat(len) { idx ->
            add(jsContextValueToKotlin(jsArrayGet(arr, idx)))
        }
    }
}

private fun normalizeJsNumber(d: Double): Number {
    if (!d.isFinite()) {
        return d
    }
    val asLong = d.toLong()
    if (asLong.toDouble() == d && asLong in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
        return asLong.toInt()
    }
    return d
}

private fun Any.contextPayloadToJs(): JsAny? =
    when (this) {
        is Map<*, *> -> {
            val nested = newJsObject()
            for ((rawKey, rawValue) in this) {
                val key = rawKey as? String ?: continue
                val concrete = rawValue ?: continue
                jsSetProperty(nested, key, concrete.contextPayloadToJs())
            }
            nested
        }

        else -> this.toJsPropertyValue()
    }
