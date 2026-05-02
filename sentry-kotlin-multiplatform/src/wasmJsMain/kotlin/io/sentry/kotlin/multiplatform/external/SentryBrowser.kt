package io.sentry.kotlin.multiplatform.external

import kotlin.js.JsAny
import kotlin.js.JsModule

/** Subset of @sentry/browser BrowserOptions used from KMP. */
@Suppress("KotlinExternalInheritance") // JsAny marker base for wasm interop
internal external interface BrowserOptions : JsAny {
    var dsn: String?
    var environment: String?
    var release: String?
    var dist: String?
    var debug: Boolean?
    var sampleRate: Double?
    var tracesSampleRate: Double?
    var maxBreadcrumbs: Int?
    var attachStacktrace: Boolean?
    var sendDefaultPii: Boolean?
    var enableAutoSessionTracking: Boolean?
    var sessionTrackingIntervalMillis: Double?
    var maxAttachmentSize: Double?
    /** Integration instances or constructors — populated in Phase 2b when wiring init. */
    var integrations: JsAny?
    var attachThreads: Boolean?
    var beforeSend: ((JsAny, JsAny) -> JsAny?)?
    var beforeBreadcrumb: ((JsAny, JsAny?) -> JsAny?)?
}

@Suppress("KotlinExternalInheritance")
internal external interface BrowserUser : JsAny {
    var id: String?
    var email: String?
    var username: String?
    var ip_address: String?
}

@Suppress("KotlinExternalInheritance")
internal external interface BrowserBreadcrumb : JsAny {
    var type: String?
    var category: String?
    var message: String?
    var level: String?
    var data: JsAny?
    var timestamp: Double?
}

@Suppress("KotlinExternalInheritance")
internal external interface BrowserScope : JsAny {
    fun setTag(key: String, value: String)
    fun setContext(key: String, context: JsAny?)
    fun setExtra(key: String, value: JsAny?)
    fun setLevel(level: String?)
    fun setUser(user: BrowserUser?)
    fun addBreadcrumb(breadcrumb: BrowserBreadcrumb)
    fun clearBreadcrumbs()
    fun clear()
}

@JsModule("@sentry/browser")
internal external object Sentry {
    fun init(options: BrowserOptions): JsAny?

    fun captureMessage(message: String): String

    fun captureMessage(message: String, level: String): String

    fun captureException(exception: JsAny): String

    fun captureFeedback(feedback: JsAny): String

    fun setUser(user: BrowserUser?)

    fun setTag(key: String, value: String)

    fun setExtra(key: String, value: JsAny?)

    fun setContext(name: String, context: JsAny?)

    fun addBreadcrumb(breadcrumb: BrowserBreadcrumb)

    fun getCurrentScope(): JsAny

    fun withScope(callback: (JsAny) -> Unit)

    /** Returns Promise<boolean>. */
    fun close(): JsAny

    /** Returns Promise<boolean>. */
    fun flush(timeout: Int): JsAny

    fun getClient(): JsAny?
}
