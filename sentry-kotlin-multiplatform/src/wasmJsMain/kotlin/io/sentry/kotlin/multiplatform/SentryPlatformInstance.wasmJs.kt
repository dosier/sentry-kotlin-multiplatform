package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.external.Sentry as JsSentry

internal actual class SentryPlatformInstance : SentryInstance {
    actual override fun init(configuration: PlatformOptionsConfiguration) {
        val options = SentryPlatformOptions()
        configuration(options)
        JsSentry.init(options.browserOptions)
    }
}
