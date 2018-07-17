package com.mercadopago.android.px.plugins.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.components.Renderer;

public class MainPaymentRenderer extends Renderer<MainPayment> {

    @Override
    public View render(@NonNull final MainPayment component, @NonNull final Context context, final ViewGroup parent) {
        return inflate(com.mercadopago.android.px.R.layout.px_view_progress_bar, parent);
    }
}