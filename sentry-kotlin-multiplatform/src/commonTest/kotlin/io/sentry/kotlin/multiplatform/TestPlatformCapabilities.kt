package io.sentry.kotlin.multiplatform

/**
 * Pathname-based attachments and the JVM/Cocoa attachment constructors are not implemented for
 * wasmJs ([Attachment.wasmJs] is a stub; @sentry/browser has no local-file attachment path).
 */
internal expect val attachmentsSupported: Boolean

/**
 * Structured logs ([SentryOptions.logs], [Sentry.logger] → [SentryLog], `logs.beforeSend`) are not
 * wired through the wasmJs bridge yet (browser SDK + KMP mapping Phase 2b).
 */
internal expect val structuredLogsSupported: Boolean
