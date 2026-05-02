package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.SentryOptions
import io.sentry.kotlin.multiplatform.external.BrowserOptions
import io.sentry.kotlin.multiplatform.external.newJsObject
import kotlin.js.unsafeCast

/**
 * Maps shared [SentryOptions] onto [@sentry/browser] init options.
 *
 * Not mapped here (native-only or non-browser): capture failed-requests tuning,
 * app hangs / watchdog termination (Cocoa), ANR (Android), attachScreenshot /
 * attachViewHierarchy, sessionReplay, ProGuard UUID, structured-log hooks
 * (`logs.beforeSend`), and other JVM/Android/Cocoa-specific knobs.
 *
 * [SentryOptions.diagnosticLevel] is also skipped — browser SDK configures SDK
 * logging separately from event severity.
 *
 * TODO(phase 2b): wire [SentryOptions.beforeSend] / [SentryOptions.beforeBreadcrumb]
 * round-trip (JS Event ↔ KMP [io.sentry.kotlin.multiplatform.SentryEvent]) via the bridge.
 */
internal fun SentryOptions.toBrowserOptions(): BrowserOptions {
    val o = newJsObject().unsafeCast<BrowserOptions>()
    o.dsn = dsn
    o.environment = environment
    o.release = release
    o.dist = dist
    o.debug = debug
    o.sampleRate = sampleRate
    o.tracesSampleRate = tracesSampleRate
    o.maxBreadcrumbs = maxBreadcrumbs
    o.attachStacktrace = attachStackTrace
    o.sendDefaultPii = sendDefaultPii
    o.enableAutoSessionTracking = enableAutoSessionTracking
    o.sessionTrackingIntervalMillis = sessionTrackingIntervalMillis.toDouble()
    o.maxAttachmentSize = maxAttachmentSize.toDouble()
    o.attachThreads = attachThreads
    o.integrations = null

    // Stubbed until Phase 2b provides JS↔KMP translation inside callbacks.
    o.beforeSend = null
    o.beforeBreadcrumb = null

    return o
}
