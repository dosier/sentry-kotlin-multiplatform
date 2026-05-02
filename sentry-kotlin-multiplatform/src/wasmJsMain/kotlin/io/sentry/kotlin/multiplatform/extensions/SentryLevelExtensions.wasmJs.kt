package io.sentry.kotlin.multiplatform.extensions

import io.sentry.kotlin.multiplatform.SentryLevel

internal fun SentryLevel.toBrowserSeverityLevel(): String =
    when (this) {
        SentryLevel.DEBUG -> "debug"
        SentryLevel.INFO -> "info"
        SentryLevel.WARNING -> "warning"
        SentryLevel.ERROR -> "error"
        SentryLevel.FATAL -> "fatal"
    }

internal fun String.toKmpSentryLevel(): SentryLevel =
    when (this) {
        "fatal" -> SentryLevel.FATAL
        "error" -> SentryLevel.ERROR
        "warning" -> SentryLevel.WARNING
        "info" -> SentryLevel.INFO
        "log" -> SentryLevel.INFO
        "debug" -> SentryLevel.DEBUG
        else -> SentryLevel.DEBUG
    }
