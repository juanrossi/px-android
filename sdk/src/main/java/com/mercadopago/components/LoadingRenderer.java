package com.mercadopago.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.mercadopago.R;

/**
 * Created by nfortuna on 11/1/17.
 */

public class LoadingRenderer extends Renderer<LoadingComponent> {
    @Override
    public View render(final LoadingComponent component, final Context context) {
        return LayoutInflater.from(context)
                .inflate(R.layout.mpsdk_view_progress_bar, null);
    }
}
