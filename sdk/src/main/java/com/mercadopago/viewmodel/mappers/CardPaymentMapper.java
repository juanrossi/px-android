package com.mercadopago.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Token;
import com.mercadopago.viewmodel.CardPaymentModel;
import com.mercadopago.viewmodel.OneTapModel;

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
        Issuer issuer = val.getPaymentMethods().getOneTapMetadata().getCard().getIssuer();
        PayerCost installment = val.getPaymentMethods().getOneTapMetadata().getCard().getAutoSelectedInstallment();
        return new CardPaymentModel(cardMapper.map(val), token, installment, issuer);
    }
}
