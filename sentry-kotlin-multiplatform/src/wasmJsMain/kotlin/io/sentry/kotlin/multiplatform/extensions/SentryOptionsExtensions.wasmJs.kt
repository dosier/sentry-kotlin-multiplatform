package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.SentryOptions
import io.sentry.kotlin.multiplatform.external.BrowserBreadcrumb
import io.sentry.kotlin.multiplatform.external.BrowserOptions
import io.sentry.kotlin.multiplatform.external.newJsObject
import kotlin.js.JsAny
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

    beforeSend?.let { kmpBeforeSend ->
        o.beforeSend = { jsEvent: JsAny, _: JsAny ->
            val kmpEvent = browserEventSnapshotToKmp(jsEvent)
            val result = kmpBeforeSend(kmpEvent)
            if (result != null) {
                result.applyToBrowserEvent(jsEvent)
                jsEvent
            } else {
                null
            }
        }
    }

    beforeBreadcrumb?.let { kmpBeforeBreadcrumb ->
        o.beforeBreadcrumb = { jsBc: JsAny, _: JsAny? ->
            val kmp = jsBc.unsafeCast<BrowserBreadcrumb>().toKmpBreadcrumb()
            val result = kmpBeforeBreadcrumb(kmp)
            result?.toBrowserBreadcrumb()
        }
    }

    return o
}
