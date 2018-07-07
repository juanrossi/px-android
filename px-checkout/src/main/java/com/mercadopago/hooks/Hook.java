package com.mercadopago.hooks;

import com.mercadopago.android.px.components.Component;

public abstract class Hook {

    abstract Component<HookComponent.Props, Void> createComponent();

    public boolean isEnabled() {
        return true;
    }
}