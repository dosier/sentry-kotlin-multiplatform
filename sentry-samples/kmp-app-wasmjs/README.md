# Kotlin/Wasm browser sample (`kmp-app-wasmjs`)

Minimal runnable sample for the **`wasmJs` Sentry bridge** in `sentry-kotlin-multiplatform`. It initializes Sentry with a **placeholder DSN only** — swap [`Main.kt`](shared/src/wasmJsMain/kotlin/io/sentry/kotlin/multiplatform/sample/Main.kt) `options.dsn` for your own project DSN when you want events to arrive in Sentry.

The sample adds **`kotlinx-browser`** on `wasmJsMain` only (Maven klib) so [`kotlinx.browser.document`](https://github.com/Kotlin/kotlinx-browser) and DOM types resolve; `@sentry/*` continues to come from the SDK via Kotlin/JS npm aggregation (no extra npm packages declared here).

## Run (dev server)

From the repo root:

```bash
./gradlew :sentry-samples:kmp-app-wasmjs:shared:wasmJsBrowserDevelopmentRun
```

Opens a local dev server (typically `http://localhost:8080`). Use the two buttons to call `captureMessage` and `captureException`.

## Build static distribution

```bash
./gradlew :sentry-samples:kmp-app-wasmjs:shared:wasmJsBrowserDistribution --no-daemon
```

Output is under `sentry-samples/kmp-app-wasmjs/shared/build/dist/wasmJs/productionExecutable/`.
