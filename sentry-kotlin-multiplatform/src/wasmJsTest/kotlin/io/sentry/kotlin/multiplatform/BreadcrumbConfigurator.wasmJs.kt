package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.extensions.browserEventSnapshotToKmp
import io.sentry.kotlin.multiplatform.extensions.toBrowserBreadcrumb
import io.sentry.kotlin.multiplatform.extensions.toBrowserOptions
import io.sentry.kotlin.multiplatform.extensions.toKmpBreadcrumb
import io.sentry.kotlin.multiplatform.external.BrowserBreadcrumb
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import kotlin.js.JsAny
import kotlin.js.unsafeCast

actual class BreadcrumbConfigurator {
    private val seed = Breadcrumb()

    actual val originalBreadcrumb: Breadcrumb = seed

    actual fun applyOptions(optionsConfiguration: OptionsConfiguration): Breadcrumb? {
        val kmpOptions = SentryOptions()
        optionsConfiguration.invoke(kmpOptions)
        return applyOptions(kmpOptions)
    }

    actual fun applyOptions(options: SentryOptions): Breadcrumb? {
        val o = options.toBrowserOptions()
        val jsBc = seed.toBrowserBreadcrumb().unsafeCast<JsAny>()
        val result = o.beforeBreadcrumb?.invoke(jsBc, null) ?: return null
        return result.unsafeCast<BrowserBreadcrumb>().toKmpBreadcrumb()
    }
}
