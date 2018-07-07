package com.mercadopago.android.px.plugins.model;

public interface Processor {

    void process(BusinessPayment businessPayment);

    void process(GenericPayment pluginPaymentResult);
}
