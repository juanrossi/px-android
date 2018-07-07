package com.mercadopago.hooks.components;

import android.support.annotation.NonNull;

import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.hooks.HookComponent;

public class PaymentConfirm extends HookComponent<Void> {

    static {
        RendererFactory.register(PaymentConfirm.class, PaymentConfirmRenderer.class);
    }

    public PaymentConfirm(@NonNull final Props props) {
        super(props);
    }
}
