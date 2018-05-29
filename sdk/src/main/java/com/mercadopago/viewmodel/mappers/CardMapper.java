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
        OneTapMetadata oneTapMetadata = val.getPaymentMethods().getOneTapMetadata();
        CardPaymentMetadata oneTapCardMetadata = oneTapMetadata.getCard();
        PaymentMethod paymentMethod =
            val.getPaymentMethods().getPaymentMethodById(oneTapMetadata.getPaymentMethodId());
        Card card = val.getPaymentMethods().getCardById(oneTapCardMetadata.getId());
        card.setPaymentMethod(paymentMethod);
        card.setIssuer(oneTapCardMetadata.getIssuer());
        return card;
    }
}
