package io.sentry.kotlin.multiplatform.protocol

private const val ZERO_32: String = "00000000000000000000000000000000"

private fun normalizeSentryIdString(raw: String): String {
    val compact = raw.replace("-", "").lowercase()
    return if (compact.isEmpty()) ZERO_32 else compact
}

public actual class SentryId actual constructor(sentryIdString: String) {
    private val normalized: String = normalizeSentryIdString(sentryIdString)

    public actual companion object {
        public actual val EMPTY_ID: SentryId = SentryId("00000000-0000-0000-0000-000000000000")
    }

    actual override fun toString(): String = normalized
}
