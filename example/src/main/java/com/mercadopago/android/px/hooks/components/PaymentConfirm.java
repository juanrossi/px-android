package com.mercadopago.android.px.hooks.components;

import android.support.annotation.NonNull;

import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.android.px.hooks.HookComponent;

public class PaymentConfirm extends HookComponent<Void> {

    static {
        RendererFactory.register(PaymentConfirm.class, PaymentConfirmRenderer.class);
    }

    public PaymentConfirm(@NonNull final Props props) {
        super(props);
    }
}
