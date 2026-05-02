package io.sentry.kotlin.multiplatform.sample

import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement

fun main() {
    Sentry.init { options ->
        options.dsn = "https://examplePublicKey@o0.ingest.sentry.io/0"
        options.environment = "wasmjs-sample"
        options.debug = true
    }

    (document.getElementById("captureMessage") ?: error("missing #captureMessage"))
        .unsafeCast<HTMLButtonElement>()
        .addEventListener(type = "click", callback = { Sentry.captureMessage("hello from wasmJs") })

    (document.getElementById("captureException") ?: error("missing #captureException"))
        .unsafeCast<HTMLButtonElement>()
        .addEventListener(
            type = "click",
            callback = {
                try {
                    throw RuntimeException("boom from wasmJs")
                } catch (e: RuntimeException) {
                    Sentry.captureException(e)
                }
            },
        )

    (document.getElementById("status") ?: error("missing #status"))
        .unsafeCast<HTMLElement>().textContent = "Sentry initialized: ${Sentry.isEnabled()}"
}
