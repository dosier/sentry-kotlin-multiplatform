package io.sentry.kotlin.multiplatform

/** Tracks init/close pairing on wasmJs; [JsSentry.close] is async but tests need sync [Sentry.isEnabled] semantics. */
internal object WasmSentrySession {
    var active: Boolean = false
}
