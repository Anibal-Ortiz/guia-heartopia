package com.guiaheartopia.app

import com.guiaheartopia.app.ads.AdsMode
import com.guiaheartopia.app.ads.AdsUiState
import com.guiaheartopia.app.ads.ConsentDecision
import com.guiaheartopia.app.ads.ConsentOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Simulación del flujo de consentimiento para un usuario de España (EEE).
 *
 * UMP decide la geografía, así que aquí se modelan sus dos respuestas posibles:
 * `canRequestAds = true` tras aceptar, `false` tras rechazar. Se comprueba QUE
 * modo de anuncios resulta y que el banner queda oculto cuando no hay base legal.
 */
class ConsentSimulationTest {

    private fun bannerVisible(mode: AdsMode): Boolean =
        AdsUiState(mode = mode, initialized = true).canShowBanner(adFree = false)

    @Test
    fun `espana acepta el consentimiento y se muestran anuncios personalizados`() {
        val mode = ConsentDecision.mode(
            outcome = ConsentOutcome.CAN_REQUEST,
            canRequestAds = true,
            personalizedAds = true,
        )

        assertEquals(AdsMode.PERSONALIZED, mode)
        assertTrue(ConsentDecision.mayRequestAds(mode))
        assertTrue(bannerVisible(mode))
    }

    @Test
    fun `espana acepta pero la politica es no personalizada y se envia npa`() {
        val mode = ConsentDecision.mode(
            outcome = ConsentOutcome.CAN_REQUEST,
            canRequestAds = true,
            personalizedAds = false,
        )

        assertEquals(AdsMode.NON_PERSONALIZED, mode)
        assertTrue(ConsentDecision.mayRequestAds(mode))
        assertTrue(bannerVisible(mode))
    }

    @Test
    fun `espana rechaza el consentimiento y no se pide ningun anuncio`() {
        val mode = ConsentDecision.mode(
            outcome = ConsentOutcome.CAN_REQUEST,
            canRequestAds = false,
            personalizedAds = true,
        )

        assertEquals(AdsMode.NOT_CONSENTED, mode)
        assertFalse(ConsentDecision.mayRequestAds(mode))
        assertFalse(bannerVisible(mode))
    }

    @Test
    fun `falla el formulario de consentimiento y no se pide ningun anuncio`() {
        val mode = ConsentDecision.mode(
            outcome = ConsentOutcome.FAILED,
            canRequestAds = false,
            personalizedAds = true,
        )

        assertEquals(AdsMode.FAILED, mode)
        assertFalse(ConsentDecision.mayRequestAds(mode))
        assertFalse(bannerVisible(mode))
    }

    @Test
    fun `fuera de la eea no se requiere consentimiento y hay anuncios`() {
        val mode = ConsentDecision.mode(
            outcome = ConsentOutcome.CAN_REQUEST,
            canRequestAds = true,
            personalizedAds = true,
        )

        assertEquals(AdsMode.PERSONALIZED, mode)
        assertTrue(bannerVisible(mode))
    }

    @Test
    fun `el usuario revoca el consentimiento en opciones de privacidad`() {
        val mode = ConsentDecision.mode(
            outcome = ConsentOutcome.NOT_CONSENTED,
            canRequestAds = false,
            personalizedAds = true,
        )

        assertEquals(AdsMode.NOT_CONSENTED, mode)
        assertFalse(ConsentDecision.mayRequestAds(mode))
        assertFalse(bannerVisible(mode))
    }

    @Test
    fun `tras un fallo un reintento correcto vuelve a anuncios`() {
        val fallo = ConsentDecision.mode(ConsentOutcome.FAILED, false, true)
        assertFalse(ConsentDecision.mayRequestAds(fallo))

        val reintento = ConsentDecision.mode(ConsentOutcome.CAN_REQUEST, true, true)

        assertEquals(AdsMode.PERSONALIZED, reintento)
        assertTrue(bannerVisible(reintento))
    }
}
