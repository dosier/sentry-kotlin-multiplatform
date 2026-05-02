// TODO(#xxx): Replace with real @sentry/browser delegation in Phase 2.

package io.sentry.kotlin.multiplatform

internal actual class SentryPlatformInstance : SentryInstance {
    actual override fun init(configuration: PlatformOptionsConfiguration) {
        // No-op
    }
}
