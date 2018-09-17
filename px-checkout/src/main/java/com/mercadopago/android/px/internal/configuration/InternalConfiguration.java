package com.mercadopago.android.px.internal.configuration;

/**
 * Internal configuration provides support to money in flow for custom checkout functionality/configure special behaviour
 * when checkout is running.
 * {@see <a href="https://github.com/mercadolibre/fury_moneyin-android">Money In repository</a>}
 */
@SuppressWarnings("unused")
public class InternalConfiguration {

    private final boolean exitOnPaymentMethodChange;

    /**
     * Constructor for internal configuration
     *
     * @param exitOnPaymentMethodChange enable to do not show payment method selection when
     * payment method is changed.
     * If set as true, then the checkout will finish when payment method change.
     * If set as false, then the checkout will not finish when payment method change.
     */
    public InternalConfiguration(final boolean exitOnPaymentMethodChange) {
        this.exitOnPaymentMethodChange = exitOnPaymentMethodChange;
    }

    /**
     * Let us know if checkout should exit on payment method change.
     *
     * @return bool which depends on checkout has to finish after payment method change.
     * If return true, then the checkout should finish when payment method change.
     * If return false, then the checkout should not finish when payment method change.
     */
    public boolean shouldExitOnPaymentMethodChange() {
        return exitOnPaymentMethodChange;
    }
}
