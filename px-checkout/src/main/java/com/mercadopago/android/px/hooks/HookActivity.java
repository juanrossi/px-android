package com.mercadopago.android.px.hooks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.mercadopago.android.px.components.Action;
import com.mercadopago.android.px.components.ActionDispatcher;
import com.mercadopago.android.px.components.BackAction;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.components.ComponentManager;
import com.mercadopago.android.px.components.NextAction;
import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.android.px.core.CheckoutStore;

public class HookActivity extends AppCompatActivity implements ActionDispatcher {

    public static Intent getIntent(@NonNull final Context context, @NonNull final Hook hook) {
        CheckoutStore.getInstance().setHook(hook);
        final Intent intent = new Intent(context, HookActivity.class);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RendererFactory.register(HookComponent.class, HookRenderer.class);

        final ComponentManager componentManager = new ComponentManager(this);
        final Hook hook = CheckoutStore.getInstance().getHook();

        if (hook == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        final Component component = hook.createComponent();
        component.setDispatcher(this);
        componentManager.render(component);
    }

    @Override
    public void dispatch(final Action action) {
        if (action instanceof NextAction) {
            setResult(RESULT_OK);
            finish();
        } else if (action instanceof BackAction) {
            onBackPressed();
        }
    }
}