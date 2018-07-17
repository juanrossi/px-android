package com.mercadopago.android.px.hooks.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.android.px.hooks.HookComponent;

public class PaymentTypeConfirm extends HookComponent<Void> {

    static {
        RendererFactory.register(PaymentTypeConfirm.class, PaymentTypeConfirmRenderer.class);
    }

    public PaymentTypeConfirm(@NonNull final Props props) {
        super(props);
    }
}
