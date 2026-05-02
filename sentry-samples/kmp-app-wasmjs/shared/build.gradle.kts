@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "shared.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain {
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
        }
        wasmJsMain.dependencies {
            implementation(project(":sentry-kotlin-multiplatform"))
            // DOM + kotlinx.browser.document (Maven klib — not npm)
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }
    }
}
