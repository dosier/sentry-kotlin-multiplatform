package io.sentry.kotlin.multiplatform.external

import kotlin.js.JsAny
import kotlin.js.unsafeCast

internal fun newJsObject(): JsAny = js("({})")

internal fun jsArray(): JsAny = js("([])")

/** Pushes a value into a JS array. */
internal fun jsArrayPush(array: JsAny, value: JsAny): Unit = js("(array.push(value), undefined)")

/** Sets a property on a JS object by name. */
internal fun jsSetProperty(target: JsAny, name: String, value: JsAny?): Unit =
    js("(target[name] = value, undefined)")

internal fun jsGetProperty(target: JsAny, name: String): JsAny? = js("target[name]")

/** Returns a shallow list of own string keys on a JS object (for interop-only objects). */
internal fun jsOwnStringKeys(target: JsAny): JsAny = js("Object.keys(target)")

internal fun jsArrayLength(array: JsAny): Int = js("array.length")

internal fun jsArrayGet(array: JsAny, index: Int): JsAny? = js("array[index]")

internal fun jsTypeof(value: JsAny?): String = js("typeof value")

internal fun jsValueToString(value: JsAny?): String = js("String(value)")

@Suppress("UNUSED_PARAMETER")
internal fun jsValueToDoubleStrict(value: JsAny?): Double = js("(Number(value))")

@Suppress("UNUSED_PARAMETER")
internal fun jsValueToBooleanStrict(value: JsAny?): Boolean = js("(Boolean(value))")

internal fun JsAny?.toKotlinDataValue(): Any? {
    if (this == null) return null
    return when (jsTypeof(this)) {
        "string" -> jsValueToString(this)
        "number" -> jsValueToDoubleStrict(this)
        "boolean" -> jsValueToBooleanStrict(this)
        else -> jsValueToString(this)
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun kotlinStringAsJs(value: String): JsAny = js("(value)")

@Suppress("UNUSED_PARAMETER")
internal fun kotlinBooleanAsJs(value: Boolean): JsAny = js("(value)")

@Suppress("UNUSED_PARAMETER")
internal fun kotlinDoubleAsJs(value: Double): JsAny = js("(value)")

@Suppress("UNUSED_PARAMETER")
internal fun kotlinIntAsJs(value: Int): JsAny = js("(value)")

/** Converts Kotlin values suitable for breadcrumb / context data into storeable JS values. */
internal fun Any?.toJsPropertyValue(): JsAny? {
    if (this == null) return null
    return when (this) {
        is String -> kotlinStringAsJs(this)
        is Boolean -> kotlinBooleanAsJs(this)
        is Int -> kotlinIntAsJs(this)
        is Short -> kotlinIntAsJs(this.toInt())
        is Byte -> kotlinIntAsJs(this.toInt())
        is Long -> kotlinDoubleAsJs(this.toDouble())
        is Float -> kotlinDoubleAsJs(this.toDouble())
        is Double -> kotlinDoubleAsJs(this)
        else -> kotlinStringAsJs(this.toString())
    }
}
