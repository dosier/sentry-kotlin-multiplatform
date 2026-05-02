package io.sentry.kotlin.multiplatform.external

import kotlin.js.JsAny
import kotlin.js.JsModule

@JsModule("@sentry/wasm")
internal external object SentryWasm {
    /** Returns a Sentry integration that symbolicates WASM stack frames. */
    fun wasmIntegration(): JsAny
}
