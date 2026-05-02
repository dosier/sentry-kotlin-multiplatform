package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.BuildKonfig
import io.sentry.kotlin.multiplatform.SentryOptions
import io.sentry.kotlin.multiplatform.external.BrowserBreadcrumb
import io.sentry.kotlin.multiplatform.external.BrowserOptions
import io.sentry.kotlin.multiplatform.external.jsArray
import io.sentry.kotlin.multiplatform.external.jsArrayPush
import io.sentry.kotlin.multiplatform.external.jsGetProperty
import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.kotlinStringAsJs
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

    val kmpBeforeSend = beforeSend
    o.beforeSend = { jsEvent: JsAny, _: JsAny ->
        if (kmpBeforeSend != null) {
            val kmpEvent = browserEventSnapshotToKmp(jsEvent)
            val processed = kmpBeforeSend(kmpEvent)
            if (processed == null) {
                null
            } else {
                processed.applyToBrowserEvent(jsEvent)
                injectBrowserSdkMetadata(this@toBrowserOptions, jsEvent)
                jsEvent
            }
        } else {
            injectBrowserSdkMetadata(this@toBrowserOptions, jsEvent)
            jsEvent
        }
    }

    val kmpBeforeBreadcrumb = beforeBreadcrumb
    o.beforeBreadcrumb = { jsBc: JsAny, _: JsAny? ->
        if (kmpBeforeBreadcrumb == null) {
            jsBc
        } else {
            val kmp = jsBc.unsafeCast<BrowserBreadcrumb>().toKmpBreadcrumb()
            val result = kmpBeforeBreadcrumb(kmp)
            result?.toBrowserBreadcrumb()
        }
    }

    return o
}

/**
 * Mirrors JVM [prepareForInit] / Cocoa beforeSend sdk merge: expose KMP SDK name + version and
 * package entries on the JS event so shared bridge tests can assert them.
 */
private fun injectBrowserSdkMetadata(kmpOptions: SentryOptions, jsEvent: JsAny) {
    val sdkExisting = jsGetProperty(jsEvent, "sdk")
    val sdk = sdkExisting ?: newJsObject().also { jsSetProperty(jsEvent, "sdk", it) }
    jsSetProperty(sdk, "name", kotlinStringAsJs(BuildKonfig.SENTRY_KMP_JAVA_SDK_NAME))
    jsSetProperty(sdk, "version", kotlinStringAsJs(BuildKonfig.VERSION_NAME))
    var packages = jsGetProperty(sdk, "packages")
    if (packages == null) {
        packages = jsArray()
        jsSetProperty(sdk, "packages", packages)
    }
    kmpOptions.sdk?.packages?.forEach { sdkPackage ->
        val pkg = newJsObject()
        jsSetProperty(pkg, "name", kotlinStringAsJs(sdkPackage.name))
        jsSetProperty(pkg, "version", kotlinStringAsJs(sdkPackage.version))
        jsArrayPush(packages, pkg)
    }
    val browserPkg = newJsObject()
    jsSetProperty(browserPkg, "name", kotlinStringAsJs(BuildKonfig.SENTRY_BROWSER_PACKAGE_NAME))
    jsSetProperty(browserPkg, "version", kotlinStringAsJs(BuildKonfig.SENTRY_BROWSER_VERSION))
    jsArrayPush(packages, browserPkg)
}
