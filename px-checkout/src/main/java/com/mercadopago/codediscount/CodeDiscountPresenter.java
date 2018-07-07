package com.mercadopago.codediscount;

import android.support.annotation.NonNull;
import com.mercadopago.internal.repository.AmountRepository;
import com.mercadopago.internal.repository.DiscountRepository;
import com.mercadopago.android.px.services.callbacks.Callback;
import com.mercadopago.android.px.services.exceptions.ApiException;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.mvp.MvpPresenter;

class CodeDiscountPresenter extends MvpPresenter<CodeDiscountView, DiscountRepository> {

    @NonNull
    /* default */ final DiscountRepository discountRepository;

    @NonNull
    protected final AmountRepository amountRepository;

    public CodeDiscountPresenter(@NonNull final DiscountRepository discountRepository,
        @NonNull final AmountRepository amountRepository) {
        this.discountRepository = discountRepository;
        this.amountRepository = amountRepository;
    }

    public void getDiscountForCode(final String input) {
        discountRepository.getCodeDiscount(amountRepository.getItemsAmount(), input)
                .enqueue(new Callback<Discount>() {
                    @Override
                    public void success(final Discount discount) {
                        if (isViewAttached()) {
                            getView().processSuccess(discount);
                        }
                    }

                    @Override
                    public void failure(final ApiException apiException) {
                        if (isViewAttached()) {
                            getView().processError();
                        }
                    }
                });
    }

}

