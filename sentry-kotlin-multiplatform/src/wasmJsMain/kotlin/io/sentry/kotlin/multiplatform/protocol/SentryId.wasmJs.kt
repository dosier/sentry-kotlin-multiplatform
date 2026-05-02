// TODO(#xxx): Replace with real @sentry/browser delegation in Phase 2.

package io.sentry.kotlin.multiplatform.protocol

public actual data class SentryId actual constructor(private val sentryIdString: String) {
    public actual companion object {
        public actual val EMPTY_ID: SentryId = SentryId("")
    }

    actual override fun toString(): String = sentryIdString
}
