package io.sentry.kotlin.multiplatform.log

import io.sentry.kotlin.multiplatform.external.Sentry as JsSentry

/**
 * Bridges KMP [SentryLogger] to [@sentry/browser] via [JsSentry.captureMessage].
 *
 * [captureMessage] severity must use strings compatible with the browser SDK (e.g. `warning` not `warn`).
 */
internal class JsSentryLoggerAdapter(
    logBuilderFactory: SentryLogBuilderFactory = DefaultSentryLogBuilderFactory,
) : BaseSentryLogger(logBuilderFactory) {
    override fun sendLog(level: SentryLogLevel, formatted: FormattedLog) {
        val severity = level.toBrowserCaptureSeverity()
        val body =
            if (formatted.attributes.isEmpty()) {
                formatted.body
            } else {
                // TODO(structured-logger): bind @sentry/browser structured logger / parameters API
                val suffix =
                    formatted.attributes.entries.joinToString(", ") { (key, v) ->
                        "$key=${v.value}"
                    }
                "${formatted.body} | $suffix"
            }
        JsSentry.captureMessage(body, severity)
    }
}

/** Severity strings for [JsSentry.captureMessage]; TRACE maps to `debug` (browser has no trace). */
private fun SentryLogLevel.toBrowserCaptureSeverity(): String =
    when (this) {
        SentryLogLevel.TRACE -> "debug"
        SentryLogLevel.DEBUG -> "debug"
        SentryLogLevel.INFO -> "info"
        SentryLogLevel.WARN -> "warning"
        SentryLogLevel.ERROR -> "error"
        SentryLogLevel.FATAL -> "fatal"
    }
