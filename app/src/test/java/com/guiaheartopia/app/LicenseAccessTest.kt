package com.guiaheartopia.app

import com.guiaheartopia.app.data.LicenseState
import com.guiaheartopia.app.data.MESSAGE_ACTIVATION_LIMIT
import com.guiaheartopia.app.data.MESSAGE_INVALID_CODE
import com.guiaheartopia.app.data.MESSAGE_RATE_LIMIT
import com.guiaheartopia.app.data.MESSAGE_SERVICE_UNAVAILABLE
import com.guiaheartopia.app.data.activationErrorMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class LicenseAccessTest {
    @Test
    fun activationLimitIsMappedToDeviceLimitMessage() {
        val body = """{"error":"NotPermitted","detail":"License key activation limit reached"}"""
        assertEquals(MESSAGE_ACTIVATION_LIMIT, activationErrorMessage(403, body))
        assertEquals(MESSAGE_ACTIVATION_LIMIT, activationErrorMessage(400, body))
    }

    @Test
    fun rateLimitIsNotMistakenForDeviceLimit() {
        val body = """{"detail":"Rate limit exceeded"}"""
        assertEquals(MESSAGE_RATE_LIMIT, activationErrorMessage(429, body))
    }

    @Test
    fun missingOrRevokedKeyIsInvalid() {
        assertEquals(MESSAGE_INVALID_CODE, activationErrorMessage(404, """{"detail":"ResourceNotFound"}"""))
        assertEquals(MESSAGE_INVALID_CODE, activationErrorMessage(403, """{"detail":"License key is revoked"}"""))
    }

    @Test
    fun serverErrorsAreReportedSeparately() {
        assertEquals(MESSAGE_SERVICE_UNAVAILABLE, activationErrorMessage(503, ""))
    }

    @Test
    fun maskedKeyNeverShowsTheWholeKey() {
        val longKey = LicenseState(licenseKey = "ABCD-1234-9F2A", loaded = true)
        assertEquals("••••-9F2A", longKey.maskedKey)

        val shortKey = LicenseState(licenseKey = "AB12", loaded = true)
        assertEquals("••••", shortKey.maskedKey)
    }
}
