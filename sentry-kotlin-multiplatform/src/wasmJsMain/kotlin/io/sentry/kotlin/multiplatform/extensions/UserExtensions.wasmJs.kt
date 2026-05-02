package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.external.BrowserUser
import io.sentry.kotlin.multiplatform.external.jsArrayGet
import io.sentry.kotlin.multiplatform.external.jsArrayLength
import io.sentry.kotlin.multiplatform.external.jsGetProperty
import io.sentry.kotlin.multiplatform.external.jsOwnStringKeys
import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.jsValueToString
import io.sentry.kotlin.multiplatform.external.kotlinStringAsJs
import io.sentry.kotlin.multiplatform.external.newJsObject
import io.sentry.kotlin.multiplatform.external.toJsPropertyValue
import io.sentry.kotlin.multiplatform.external.toKotlinDataValue
import io.sentry.kotlin.multiplatform.protocol.User
import kotlin.js.JsAny
import kotlin.js.unsafeCast

private val RESERVED_BROWSER_USER_KEYS =
    setOf("id", "email", "username", "ip_address")

internal fun User.toBrowserUser(): BrowserUser {
    val raw = newJsObject()
    jsSetProperty(raw, "id", id?.let { kotlinStringAsJs(it) })
    jsSetProperty(raw, "email", email?.let { kotlinStringAsJs(it) })
    jsSetProperty(raw, "username", username?.let { kotlinStringAsJs(it) })
    jsSetProperty(raw, "ip_address", ipAddress?.let { kotlinStringAsJs(it) })

    other?.forEach { (key, value) ->
        jsSetProperty(raw, key, kotlinStringAsJs(value))
    }

    unknown?.forEach { (key, value) ->
        jsSetProperty(raw, key, value.toJsPropertyValue())
    }

    return raw.unsafeCast<BrowserUser>()
}

internal fun BrowserUser.toKmpUser(): User {
    val kmp = User()
    kmp.id = id
    kmp.email = email
    kmp.username = username
    kmp.ipAddress = ip_address

    val self = this.unsafeCast<JsAny>()
    val keysArray = jsOwnStringKeys(self)
    val len = jsArrayLength(keysArray)
    var index = 0
    while (index < len) {
        val keyJs = jsArrayGet(keysArray, index)
        index++
        val key = keyJs?.let { jsValueToString(it) }?.takeUnless { it.isEmpty() } ?: continue
        if (key in RESERVED_BROWSER_USER_KEYS) continue
        val rawValue = jsGetProperty(self, key)
        val kotlinValue = rawValue.toKotlinDataValue()
        when (kotlinValue) {
            is String ->
                kmp.other = (kmp.other ?: mutableMapOf()).apply { put(key, kotlinValue) }
            null -> Unit
            else ->
                kmp.unknown = (kmp.unknown ?: mutableMapOf()).apply { put(key, kotlinValue) }
        }
    }
    return kmp
}
