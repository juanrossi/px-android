package com.mercadopago.android.px.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.OneTapMetadata;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.viewmodel.CardPaymentModel;
import com.mercadopago.android.px.viewmodel.OneTapModel;

public class CardPaymentMapper extends Mapper<OneTapModel, CardPaymentModel> {

    @NonNull
    private final Token token;
    private CardMapper cardMapper;

    public CardPaymentMapper(@NonNull final Token token) {
        this.token = token;
        cardMapper = new CardMapper();
    }

    @Override
    public CardPaymentModel map(@NonNull final OneTapModel val) {
        final OneTapMetadata metadata = val.getPaymentMethods().getOneTapMetadata();
        final Issuer issuer = val.getPaymentMethods().getIssuer(metadata.getCard().getId());
        final PayerCost payerCost = metadata.getCard().getAutoSelectedInstallment();

        return new CardPaymentModel(cardMapper.map(val), token, payerCost, issuer);
    }
}
