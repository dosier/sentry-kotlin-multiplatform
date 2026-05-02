package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.extensions.toBrowserBreadcrumb
import io.sentry.kotlin.multiplatform.extensions.toBrowserFeedbackObject
import io.sentry.kotlin.multiplatform.extensions.toBrowserUser
import io.sentry.kotlin.multiplatform.extensions.toJsError
import io.sentry.kotlin.multiplatform.external.Sentry as JsSentry
import io.sentry.kotlin.multiplatform.log.JsSentryLoggerAdapter
import io.sentry.kotlin.multiplatform.log.SentryLogger
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import io.sentry.kotlin.multiplatform.protocol.SentryId
import io.sentry.kotlin.multiplatform.protocol.User
import io.sentry.kotlin.multiplatform.protocol.UserFeedback

@Suppress("UnusedPrivateMember")
internal actual class SentryBridge actual constructor(
    private val sentryInstance: SentryInstance,
) {
    private val logger = JsSentryLoggerAdapter()

    actual fun init(context: Context, configuration: OptionsConfiguration) {
        init(configuration)
    }

    actual fun init(configuration: OptionsConfiguration) {
        val options = SentryOptions()
        configuration.invoke(options)
        initWithPlatformOptions(options.toPlatformOptionsConfiguration())
    }

    actual fun initWithPlatformOptions(configuration: PlatformOptionsConfiguration) {
        val finalConfiguration: PlatformOptionsConfiguration = {
            configuration(it)
            it.prepareForInit()
        }
        sentryInstance.init(finalConfiguration)
    }

    actual fun captureMessage(message: String): SentryId =
        SentryId(JsSentry.captureMessage(message))

    actual fun captureMessage(message: String, scopeCallback: ScopeCallback): SentryId {
        var id = SentryId.EMPTY_ID
        JsSentry.withScope {
            // TODO(scope-adapter): invoke [scopeCallback] with a [Scope] backed by the JS scope.
            // Until then the callback is not run; the message is still captured.
            id = SentryId(JsSentry.captureMessage(message))
        }
        return id
    }

    actual fun captureException(throwable: Throwable): SentryId =
        SentryId(JsSentry.captureException(throwable.toJsError()))

    actual fun captureException(throwable: Throwable, scopeCallback: ScopeCallback): SentryId {
        var id = SentryId.EMPTY_ID
        JsSentry.withScope {
            // TODO(scope-adapter): invoke [scopeCallback] with a [Scope] backed by the JS scope.
            id = SentryId(JsSentry.captureException(throwable.toJsError()))
        }
        return id
    }

    actual fun captureUserFeedback(userFeedback: UserFeedback) {
        JsSentry.captureFeedback(userFeedback.toBrowserFeedbackObject())
    }

    actual fun configureScope(scopeCallback: ScopeCallback) {
        // TODO(scope-adapter): expose JS [configureScope] through a KMP [Scope] adapter;
        // until then prefer JsSentry.setUser / setTag / setExtra / setContext from app code.
    }

    actual fun addBreadcrumb(breadcrumb: Breadcrumb) {
        JsSentry.addBreadcrumb(breadcrumb.toBrowserBreadcrumb())
    }

    actual fun setUser(user: User?) {
        JsSentry.setUser(user?.toBrowserUser())
    }

    actual fun logger(): SentryLogger = logger

    actual fun isCrashedLastRun(): Boolean = false

    actual fun isEnabled(): Boolean = JsSentry.getClient() != null

    actual fun close() {
        JsSentry.close()
    }
}
