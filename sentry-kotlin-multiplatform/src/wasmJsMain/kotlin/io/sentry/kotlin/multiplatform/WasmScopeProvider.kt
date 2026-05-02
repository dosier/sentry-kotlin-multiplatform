package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.extensions.toBrowserBreadcrumb
import io.sentry.kotlin.multiplatform.extensions.toBrowserSeverityLevel
import io.sentry.kotlin.multiplatform.extensions.toBrowserUser
import io.sentry.kotlin.multiplatform.external.BrowserScope
import io.sentry.kotlin.multiplatform.external.jsArray
import io.sentry.kotlin.multiplatform.external.jsArrayPush
import io.sentry.kotlin.multiplatform.external.jsSetProperty
import io.sentry.kotlin.multiplatform.external.kotlinBooleanAsJs
import io.sentry.kotlin.multiplatform.external.kotlinDoubleAsJs
import io.sentry.kotlin.multiplatform.external.kotlinIntAsJs
import io.sentry.kotlin.multiplatform.external.kotlinStringAsJs
import io.sentry.kotlin.multiplatform.external.newJsObject
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import io.sentry.kotlin.multiplatform.protocol.User
import kotlin.js.JsAny

/**
 * Bridges KMP [Scope] calls to @sentry/browser's scope passed into
 * `Sentry.configureScope` / `Sentry.withScope`.
 *
 * Mirrors [MemoryScope]'s context shape so shared integration tests behave like JVM/Cocoa.
 */
internal class WasmScopeProvider(private val browser: BrowserScope) : Scope {

    override var user: User?
        get() = null
        set(value) {
            browser.setUser(value?.toBrowserUser())
        }

    override var level: SentryLevel?
        get() = null
        set(value) {
            if (value == null) {
                browser.setLevel(null)
            } else {
                browser.setLevel(value.toBrowserSeverityLevel())
            }
        }

    override fun getTags(): MutableMap<String, String> = mutableMapOf()

    override fun getContexts(): MutableMap<String, Any> = mutableMapOf()

    override fun addAttachment(attachment: Attachment) {
        // wasmJs attachments are not wired to the browser transport (see Attachment.wasmJs stub).
    }

    override fun clearAttachments() {
        // no-op
    }

    override fun addBreadcrumb(breadcrumb: Breadcrumb) {
        browser.addBreadcrumb(breadcrumb.toBrowserBreadcrumb())
    }

    override fun clearBreadcrumbs() {
        browser.clearBreadcrumbs()
    }

    override fun setContext(key: String, value: Any) {
        when (value) {
            is Map<*, *> -> browser.setContext(key, kotlinMapToContextJs(value))
            else -> browser.setContext(key, wrapSingleContextValue(value))
        }
    }

    override fun setContext(key: String, value: Boolean) {
        browser.setContext(key, wrapSingleContextValue(value))
    }

    override fun setContext(key: String, value: String) {
        browser.setContext(key, wrapSingleContextValue(value))
    }

    override fun setContext(key: String, value: Number) {
        browser.setContext(key, wrapSingleContextValue(value))
    }

    override fun setContext(key: String, value: Collection<*>) {
        browser.setContext(key, wrapSingleContextValue(value))
    }

    override fun setContext(key: String, value: Array<*>) {
        browser.setContext(key, wrapSingleContextValue(value.asList()))
    }

    override fun setContext(key: String, value: Char) {
        browser.setContext(key, wrapSingleContextValue(value))
    }

    override fun removeContext(key: String) {
        browser.setContext(key, null)
    }

    override fun setTag(key: String, value: String) {
        browser.setTag(key, value)
    }

    override fun removeTag(key: String) {
        // @sentry/core v10 Scope has no removeTag; not used by wasm integration tests.
    }

    override fun removeExtra(key: String) {
        // @sentry/core v10 Scope has no removeExtra; not used by wasm integration tests.
    }

    override fun setExtra(key: String, value: String) {
        browser.setExtra(key, kotlinStringAsJs(value))
    }

    override fun clear() {
        browser.clear()
    }

    private fun wrapSingleContextValue(value: Any?): JsAny {
        val o = newJsObject()
        jsSetProperty(o, "value", kotlinValueToContextJs(value))
        return o
    }

    private fun kotlinMapToContextJs(map: Map<*, *>): JsAny {
        val o = newJsObject()
        for ((rawKey, rawValue) in map) {
            val key = rawKey?.toString() ?: continue
            val concrete = rawValue ?: continue
            jsSetProperty(o, key, kotlinValueToContextJs(concrete))
        }
        return o
    }

    private fun kotlinValueToContextJs(value: Any?): JsAny? {
        if (value == null) {
            return null
        }
        return when (value) {
            is String -> kotlinStringAsJs(value)
            is Boolean -> kotlinBooleanAsJs(value)
            is Byte -> kotlinIntAsJs(value.toInt())
            is Short -> kotlinIntAsJs(value.toInt())
            is Int -> kotlinIntAsJs(value)
            is Long -> kotlinDoubleAsJs(value.toDouble())
            is Float -> kotlinDoubleAsJs(value.toDouble())
            is Double -> kotlinDoubleAsJs(value)
            is Char -> kotlinIntAsJs(value.code)
            is Map<*, *> -> kotlinMapToContextJs(value)
            is Array<*> -> kotlinCollectionToJsArray(value.asList())
            is Collection<*> -> kotlinCollectionToJsArray(value)
            else -> kotlinStringAsJs(value.toString())
        }
    }

    private fun kotlinCollectionToJsArray(collection: Collection<*>): JsAny {
        val arr = jsArray()
        for (element in collection) {
            kotlinValueToContextJs(element)?.let { jsArrayPush(arr, it) }
        }
        return arr
    }
}
