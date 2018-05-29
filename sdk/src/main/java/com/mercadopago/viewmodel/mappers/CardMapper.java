package com.mercadopago.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.model.Card;
import com.mercadopago.model.CardPaymentMetadata;
import com.mercadopago.model.OneTapMetadata;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.viewmodel.OneTapModel;

public class CardMapper extends Mapper<OneTapModel, Card> {

    @Override
    public Card map(@NonNull final OneTapModel val) {
        OneTapMetadata oneTapMetadata = val.paymentMethods.getOneTapMetadata();
        CardPaymentMetadata oneTapCardMetadata = oneTapMetadata.card;
        PaymentMethod paymentMethod =
            val.paymentMethods.getPaymentMethodById(oneTapMetadata.paymentMethodId);
        Card card = val.paymentMethods.getCardById(oneTapCardMetadata.id);
        card.setPaymentMethod(paymentMethod);
        card.setIssuer(oneTapCardMetadata.issuer);
        return card;
    }
}
