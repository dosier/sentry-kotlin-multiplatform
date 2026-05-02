package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.protocol.Breadcrumb

actual data class BreadcrumbTestConverter actual constructor(val breadcrumb: Breadcrumb) {

    actual fun getType(): String? = breadcrumb.type

    actual fun getCategory(): String? = breadcrumb.category

    actual fun getMessage(): String? = breadcrumb.message

    actual fun getData(): MutableMap<String, Any> = breadcrumb.getData() ?: mutableMapOf()

    actual fun getLevel(): SentryLevel? = breadcrumb.level
}
