// TODO(#xxx): Replace with real @sentry/browser delegation in Phase 2.

package io.sentry.kotlin.multiplatform

public actual class SentryPlatformOptions

internal actual fun SentryPlatformOptions.prepareForInit() {
    // No-op
}

internal actual fun SentryOptions.toPlatformOptionsConfiguration(): PlatformOptionsConfiguration =
    {
        // No-op
    }
