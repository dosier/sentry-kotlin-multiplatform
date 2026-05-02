package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.external.BrowserBreadcrumb
import io.sentry.kotlin.multiplatform.external.jsArrayGet
import io.sentry.kotlin.multiplatform.external.jsArrayLength
import io.sentry.kotlin.multiplatform.external.jsGetProperty
import io.sentry.kotlin.multiplatform.external.jsOwnStringKeys
import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.jsValueToString
import io.sentry.kotlin.multiplatform.external.newJsObject
import io.sentry.kotlin.multiplatform.external.toJsPropertyValue
import io.sentry.kotlin.multiplatform.external.toKotlinDataValue
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import kotlin.js.JsAny
import kotlin.js.unsafeCast

internal fun Breadcrumb.toBrowserBreadcrumb(): BrowserBreadcrumb {
    val raw = newJsObject().unsafeCast<BrowserBreadcrumb>()
    raw.type = type
    raw.category = category
    raw.message = message
    raw.level = level?.toBrowserSeverityLevel()

    val dataMap = getData()
    if (dataMap != null && dataMap.isNotEmpty()) {
        val dataObj = newJsObject()
        dataMap.forEach { (key, value) ->
            jsSetProperty(dataObj, key, value.toJsPropertyValue())
        }
        raw.data = dataObj
    }
    return raw
}

internal fun BrowserBreadcrumb.toKmpBreadcrumb(): Breadcrumb {
    val kmp = Breadcrumb()
    kmp.message = message
    kmp.type = type
    kmp.category = category
    kmp.level = level?.toKmpSentryLevel()

    val dataJs = data ?: return kmp
    val map = mutableMapOf<String, Any>()
    val keys = jsOwnStringKeys(dataJs.unsafeCast())
    val len = jsArrayLength(keys)
    var i = 0
    while (i < len) {
        val keyJs = jsArrayGet(keys, i)
        i++
        val key =
            keyJs?.let { jsValueToString(it) }?.takeUnless { it.isEmpty() }
                ?: continue
        val v =
            jsGetProperty(dataJs.unsafeCast(), key)
                .toKotlinDataValue()
        if (v != null) {
            map[key] = v
        }
    }
    if (map.isNotEmpty()) {
        kmp.setData(map)
    }
    return kmp
}
