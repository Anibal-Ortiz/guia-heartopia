package com.guiaheartopia.app.ads

/**
 * Decisión de anuncios a partir del resultado de UMP.
 *
 * Se mantiene separada de [AdsController] (y sin clases de Android) para poder
 * simular en tests los escenarios del Espacio Económico Europeo: consentimiento
 * concedido, rechazado o error en el formulario.
 */
object ConsentDecision {
    /**
     * @param outcome resultado devuelto por el gateway de UMP.
     * @param canRequestAds valor de `ConsentInformation.canRequestAds()` tras
     *   `requestConsentInfoUpdate()` y el formulario (false si el usuario rechaza).
     * @param personalizedAds política de personalización de esta compilación.
     */
    fun mode(
        outcome: ConsentOutcome,
        canRequestAds: Boolean,
        personalizedAds: Boolean,
    ): AdsMode = when (outcome) {
        ConsentOutcome.CAN_REQUEST -> when {
            !canRequestAds -> AdsMode.NOT_CONSENTED
            personalizedAds -> AdsMode.PERSONALIZED
            else -> AdsMode.NON_PERSONALIZED
        }
        ConsentOutcome.NOT_CONSENTED -> AdsMode.NOT_CONSENTED
        ConsentOutcome.FAILED -> AdsMode.FAILED
    }

    /** True solo en los dos modos en los que se permite pedir anuncios. */
    fun mayRequestAds(mode: AdsMode): Boolean =
        mode == AdsMode.PERSONALIZED || mode == AdsMode.NON_PERSONALIZED
}
