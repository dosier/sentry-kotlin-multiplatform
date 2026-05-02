package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.extensions.toBrowserOptions
import io.sentry.kotlin.multiplatform.external.BrowserOptions
import io.sentry.kotlin.multiplatform.external.SentryWasm
import io.sentry.kotlin.multiplatform.external.jsArray
import io.sentry.kotlin.multiplatform.external.jsArrayPush
import io.sentry.kotlin.multiplatform.external.jsAssign
import io.sentry.kotlin.multiplatform.external.jsGetProperty
import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.newJsObject
import kotlin.js.JsAny
import kotlin.js.unsafeCast

public actual class SentryPlatformOptions {
    internal val browserOptions: BrowserOptions =
        newJsObject().unsafeCast<BrowserOptions>()
}

internal actual fun SentryPlatformOptions.prepareForInit() {
    // Append @sentry/wasm so Wasm stack frames symbolicate. If [integrations] was unset,
    // this yields an array containing only wasmIntegration (see Phase 3+ to merge SDK defaults).
    val target = browserOptions.unsafeCast<JsAny>()
    val existing = jsGetProperty(target, "integrations")
    val integrations = existing ?: jsArray()
    jsArrayPush(integrations, SentryWasm.wasmIntegration())
    jsSetProperty(target, "integrations", integrations)
}

internal actual fun SentryOptions.toPlatformOptionsConfiguration(): PlatformOptionsConfiguration =
    { platformOptions ->
        jsAssign(
            platformOptions.browserOptions.unsafeCast<JsAny>(),
            toBrowserOptions().unsafeCast<JsAny>(),
        )
    }
