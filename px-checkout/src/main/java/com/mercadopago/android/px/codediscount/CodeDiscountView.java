package com.mercadopago.android.px.codediscount;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.mvp.MvpView;

public interface CodeDiscountView extends MvpView {

    void showCodeError();

    void processSuccess(@NonNull final Discount discount);

    void processError();
}
