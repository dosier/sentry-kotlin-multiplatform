package io.sentry.kotlin.multiplatform

import io.sentry.kotlin.multiplatform.external.BrowserOptions
import io.sentry.kotlin.multiplatform.external.jsArrayGet
import io.sentry.kotlin.multiplatform.external.jsArrayLength
import io.sentry.kotlin.multiplatform.external.jsGetProperty
import io.sentry.kotlin.multiplatform.external.jsValueToString
import io.sentry.kotlin.multiplatform.external.newJsObject
import io.sentry.kotlin.multiplatform.fakes.FakeSentryInstance
import io.sentry.kotlin.multiplatform.utils.fakeDsn
import kotlin.js.JsAny
import kotlin.js.unsafeCast
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

actual class SentryBridgeTest {
    private lateinit var fixture: Fixture

    @BeforeTest
    fun `set up`() {
        fixture = Fixture()
    }

    @Test
    actual fun `init sets correct configuration`() {
        val configuration: OptionsConfiguration = {
            it.dsn = fakeDsn
            it.release = "1.0.0"
        }

        fixture.sut.init(configuration)

        val expectedOptions = SentryOptions().apply(configuration)
        val browserOptions = platformOptionsBrowser(fixture)

        assertEquals(expectedOptions.dsn, browserOptions.dsn)
        assertEquals(expectedOptions.release, browserOptions.release)
    }

    @Test
    actual fun `setting null in beforeSend during init drops the event`() {
        fixture.sut.init {
            it.beforeSend = {
                null
            }
        }

        val browserOptions = platformOptionsBrowser(fixture)
        val beforeSend = assertNotNull(browserOptions.beforeSend)
        val emptyEvent = newJsObject()
        val result = beforeSend(emptyEvent, newJsObject())
        assertNull(result)
    }

    @Test
    actual fun `default beforeSend in init does not drop the event`() {
        fixture.sut.init { }

        val browserOptions = platformOptionsBrowser(fixture)
        val beforeSend = assertNotNull(browserOptions.beforeSend)
        val emptyEvent = newJsObject()
        val result = beforeSend(emptyEvent, newJsObject())
        assertNotNull(result)
    }

    @Test
    actual fun `default beforeSend in init does not drop the event after prepareForInit`() {
        fixture.sut.init { }

        val platformOpts = SentryPlatformOptions().apply {
            fixture.sentryInstance.lastConfiguration?.invoke(this)
            prepareForInit()
        }
        val beforeSend = assertNotNull(platformOpts.browserOptions.beforeSend)
        val emptyEvent = newJsObject()
        val result = beforeSend(emptyEvent, newJsObject())
        assertNotNull(result)
    }

    @Test
    actual fun `init sets the SDK packages`() {
        fixture.sut.init { }

        val browserOptions = platformOptionsBrowser(fixture)
        val beforeSend = assertNotNull(browserOptions.beforeSend)
        val emptyEvent = newJsObject()
        val result = beforeSend(emptyEvent, newJsObject())!!.unsafeCast<JsAny>()
        val sdk = assertNotNull(jsGetProperty(result, "sdk"))
        val packages = assertNotNull(jsGetProperty(sdk, "packages")).unsafeCast<JsAny>()
        assertTrue(jsArrayLength(packages) > 0)
        var found = false
        var i = 0
        while (i < jsArrayLength(packages)) {
            val pkg = jsArrayGet(packages, i)!!
            val name = jsValueToString(jsGetProperty(pkg, "name"))
            if (name.contains("sentry", ignoreCase = true)) {
                found = true
                break
            }
            i++
        }
        assertTrue(found)
    }

    @Test
    actual fun `init sets SDK version and name`() {
        val configuration: OptionsConfiguration = {
            it.dsn = fakeDsn
            it.release = "1.0.0"
        }

        fixture.sut.init(configuration)

        val browserOptions = platformOptionsBrowser(fixture)
        val beforeSend = assertNotNull(browserOptions.beforeSend)
        val emptyEvent = newJsObject()
        val result = beforeSend(emptyEvent, newJsObject())!!.unsafeCast<JsAny>()
        val sdk = assertNotNull(jsGetProperty(result, "sdk"))
        val name = jsValueToString(jsGetProperty(sdk, "name"))
        val version = jsValueToString(jsGetProperty(sdk, "version"))
        assertTrue(name.contains("kmp"))
        assertEquals(BuildKonfig.VERSION_NAME, version)
    }

    private fun platformOptionsBrowser(f: Fixture): BrowserOptions =
        SentryPlatformOptions()
            .apply {
                f.sentryInstance.lastConfiguration!!(this)
            }.browserOptions

    internal class Fixture {
        val sentryInstance = FakeSentryInstance()

        val sut get() = SentryBridge(sentryInstance)
    }
}
