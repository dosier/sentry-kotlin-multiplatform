package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.utils.fakeDsn
import kotlin.test.assertEquals

actual interface PlatformOptions : CommonPlatformOptions

private class WasmPlatformOptionsWrapper(
    private val platformOpts: SentryPlatformOptions = SentryPlatformOptions(),
) : PlatformOptions {
    private var diagnosticLevelCached: SentryLevel = SentryLevel.DEBUG
    private var proguardUuidCached: String? = null

    override val dsn: String?
        get() = platformOpts.browserOptions.dsn

    override val attachStackTrace: Boolean
        get() = platformOpts.browserOptions.attachStacktrace ?: true

    override val release: String?
        get() = platformOpts.browserOptions.release

    override val debug: Boolean
        get() = platformOpts.browserOptions.debug ?: false

    override val environment: String?
        get() = platformOpts.browserOptions.environment

    override val diagnosticLevel: SentryLevel
        get() = diagnosticLevelCached

    override val dist: String?
        get() = platformOpts.browserOptions.dist

    override val enableAutoSessionTracking: Boolean
        get() = platformOpts.browserOptions.enableAutoSessionTracking ?: true

    override val sessionTrackingIntervalMillis: Long
        get() = (platformOpts.browserOptions.sessionTrackingIntervalMillis ?: 30000.0).toLong()

    override val maxBreadcrumbs: Int
        get() = platformOpts.browserOptions.maxBreadcrumbs ?: DEFAULT_MAX_BREADCRUMBS

    override val maxAttachmentSize: Long
        get() = (platformOpts.browserOptions.maxAttachmentSize ?: DEFAULT_MAX_ATTACHMENT_SIZE.toDouble()).toLong()

    override val sampleRate: Double?
        get() = platformOpts.browserOptions.sampleRate

    override val tracesSampleRate: Double?
        get() = platformOpts.browserOptions.tracesSampleRate

    override val sendDefaultPii: Boolean
        get() = platformOpts.browserOptions.sendDefaultPii ?: false

    override val proguardUuid: String?
        get() = proguardUuidCached

    override fun applyFromOptions(options: SentryOptions) {
        diagnosticLevelCached = options.diagnosticLevel
        proguardUuidCached = options.proguardUuid
        options.toPlatformOptionsConfiguration().invoke(platformOpts)
    }
}

actual fun createPlatformOptions(): PlatformOptions = WasmPlatformOptionsWrapper()

actual fun createSentryPlatformOptionsConfiguration(): PlatformOptionsConfiguration = {
    val o = it as SentryPlatformOptions
    o.browserOptions.dsn = fakeDsn
}

actual fun PlatformOptions.assertPlatformSpecificOptions(kmpOptions: SentryOptions) {
    assertEquals(proguardUuid, kmpOptions.proguardUuid)
}
