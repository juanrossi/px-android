package com.mercadopago.codediscount;

import android.support.annotation.NonNull;

import com.mercadopago.model.Discount;
import com.mercadopago.mvp.MvpView;

public interface CodeDiscountView extends MvpView {

    void showCodeError();

    void processSuccess(@NonNull final Discount discount);

    void processError();

}
