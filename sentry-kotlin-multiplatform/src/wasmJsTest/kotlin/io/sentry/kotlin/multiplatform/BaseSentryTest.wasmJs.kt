package io.sentry.kotlin.multiplatform

actual abstract class BaseSentryTest {
    actual val platform: String = "wasmJs"

    actual val authToken: String? = null

    actual fun sentryInit(optionsConfiguration: OptionsConfiguration) {
        Sentry.init(optionsConfiguration)
    }

    actual fun sentryInitWithPlatformOptions(platformOptionsConfiguration: PlatformOptionsConfiguration) {
        Sentry.initWithPlatformOptions(platformOptionsConfiguration)
    }
}
