package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import io.sentry.kotlin.multiplatform.protocol.User

/**
 * In-memory [Scope] for wasm tests — mirrors JVM/Cocoa scope shape expected by [ScopeTest].
 */
internal class MemoryScope : Scope {
    private val tags = mutableMapOf<String, String>()
    private val contexts = mutableMapOf<String, Any>()
    private val extras = mutableMapOf<String, String>()
    private val attachments = mutableListOf<Attachment>()
    private val breadcrumbs = mutableListOf<Breadcrumb>()

    private var backingUser: User? = null

    override var user: User?
        get() = backingUser?.let { User(it) }
        set(value) {
            backingUser = value?.let { User(it) }
        }

    override var level: SentryLevel? = null

    override fun getTags(): MutableMap<String, String> = tags

    override fun getContexts(): MutableMap<String, Any> = contexts

    override fun addAttachment(attachment: Attachment) {
        attachments.add(attachment)
    }

    override fun clearAttachments() {
        attachments.clear()
    }

    override fun addBreadcrumb(breadcrumb: Breadcrumb) {
        breadcrumbs.add(breadcrumb)
    }

    override fun clearBreadcrumbs() {
        breadcrumbs.clear()
    }

    override fun setContext(key: String, value: Any) {
        contexts[key] =
            when (value) {
                is Map<*, *> -> value
                else -> mapOf("value" to value)
            }
    }

    override fun setContext(key: String, value: Boolean) {
        contexts[key] = mapOf("value" to value)
    }

    override fun setContext(key: String, value: String) {
        contexts[key] = mapOf("value" to value)
    }

    override fun setContext(key: String, value: Number) {
        contexts[key] = mapOf("value" to value)
    }

    override fun setContext(key: String, value: Collection<*>) {
        contexts[key] = mapOf("value" to value)
    }

    override fun setContext(key: String, value: Array<*>) {
        contexts[key] = mapOf("value" to value)
    }

    override fun setContext(key: String, value: Char) {
        contexts[key] = mapOf("value" to value)
    }

    override fun removeContext(key: String) {
        contexts.remove(key)
    }

    override fun setTag(key: String, value: String) {
        tags[key] = value
    }

    override fun removeTag(key: String) {
        tags.remove(key)
    }

    override fun setExtra(key: String, value: String) {
        extras[key] = value
    }

    override fun removeExtra(key: String) {
        extras.remove(key)
    }

    override fun clear() {
        backingUser = null
        level = null
        tags.clear()
        contexts.clear()
        extras.clear()
        attachments.clear()
        breadcrumbs.clear()
    }
}
