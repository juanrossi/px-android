package com.mercadopago.util.textformatter;

import android.text.Spannable;

abstract class ChainFormatter {

    protected abstract Spannable apply(CharSequence charSequence);
}
