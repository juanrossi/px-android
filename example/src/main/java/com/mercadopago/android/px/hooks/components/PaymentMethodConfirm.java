package com.mercadopago.android.px.hooks.components;

import android.support.annotation.NonNull;

import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.android.px.hooks.HookComponent;

public class PaymentMethodConfirm extends HookComponent<Void> {

    static {
        RendererFactory.register(PaymentMethodConfirm.class, PaymentMethodConfirmRenderer.class);
    }

    public PaymentMethodConfirm(@NonNull final Props props) {
        super(props);
    }
}
