package com.mercadopago.android.px.util.textformatter;

import android.text.Spannable;

abstract class ChainFormatter {

    protected abstract Spannable apply(CharSequence charSequence);
}
