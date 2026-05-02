package io.sentry.kotlin.multiplatform.extensions

import kotlin.js.JsAny

/**
 * Converts a Kotlin [Throwable] into a JavaScript `Error` so Sentry's stack
 * parser preserves the Kotlin/Wasm stack text.
 */
internal fun Throwable.toJsError(): JsAny {
    val name = this::class.simpleName ?: "Error"
    val message = this.message ?: ""
    val stack = this.stackTraceToString()
    return makeJsError(name, message, stack)
}

@Suppress("UNUSED_PARAMETER")
private fun makeJsError(name: String, message: String, stack: String): JsAny =
    js("(function(){ var e = new Error(message); e.name = name; e.stack = stack; return e; })()")
