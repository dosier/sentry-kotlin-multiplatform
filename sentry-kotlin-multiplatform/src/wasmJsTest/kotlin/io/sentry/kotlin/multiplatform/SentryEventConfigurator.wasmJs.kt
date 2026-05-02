package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.extensions.browserEventSnapshotToKmp
import io.sentry.kotlin.multiplatform.extensions.toBrowserOptions
import io.sentry.kotlin.multiplatform.external.newJsObject
import kotlin.js.JsAny
import kotlin.js.unsafeCast

actual class SentryEventConfigurator {
    actual val originalEvent: SentryEvent = SentryEvent()

    actual fun applyOptions(optionsConfiguration: OptionsConfiguration): SentryEvent? {
        val kmpOptions = SentryOptions()
        optionsConfiguration.invoke(kmpOptions)
        return applyOptions(kmpOptions)
    }

    actual fun applyOptions(options: SentryOptions): SentryEvent? {
        val o = options.toBrowserOptions()
        val jsEvent = newJsObject().unsafeCast<JsAny>()
        val result = o.beforeSend?.invoke(jsEvent, newJsObject().unsafeCast<JsAny>())
        return if (result == null) {
            null
        } else {
            browserEventSnapshotToKmp(result.unsafeCast())
        }
    }
}
