package com.mercadopago.android.px.internal.features.guessing_card;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.Token;
import java.util.List;

public class GuessingCardStoragePresenter extends GuessingCardPresenter {

    @Override
    public void initialize() {
        //TODO: implement
    }

    @Override
    public String getPaymentTypeId() {
        return null;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return null;
    }

    @Override
    public void setPaymentMethod(@Nullable final PaymentMethod paymentMethod) {
        //TODO: implement
    }

    @Override
    public void getPaymentMethods() {
        //TODO: implement
    }

    @Override
    public void onPaymentMethodSet(final PaymentMethod paymentMethod) {
        //TODO: implement
    }

    @Override
    public void resolvePaymentMethodCleared() {
        //TODO: implement
    }

    @Override
    public void checkFinishWithCardToken() {
        //TODO: implement
    }

    @Override
    public void resolveTokenRequest(final Token token) {
        //TODO: implement
    }

    @Override
    public List<BankDeal> getBankDealsList() {
        return null;
    }

    //TODO: implement
    @Override
    public void onSaveInstanceState(final Bundle outState, final String cardSideState, final boolean lowResActive) {
        //TODO: implement
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        //TODO: implement
    }
}
