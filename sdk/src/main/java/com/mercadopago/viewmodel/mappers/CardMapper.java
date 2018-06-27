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
        final OneTapMetadata oneTapMetadata = val.getPaymentMethods().getOneTapMetadata();
        final CardPaymentMetadata cardPaymentMetadata = oneTapMetadata.getCard();
        final PaymentMethod paymentMethod =
            val.getPaymentMethods().getPaymentMethodById(oneTapMetadata.getPaymentMethodId());
        final Card card = val.getPaymentMethods().getCardById(cardPaymentMetadata.getId());
        card.setSecurityCode(paymentMethod != null ? paymentMethod.getSecurityCode() : null);
        card.setPaymentMethod(paymentMethod);
        return card;
    }
}
