package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.services.CardService;
import com.mercadopago.android.px.model.Card;
import java.util.HashMap;
import java.util.Map;

public class CardAssociationService {
    @NonNull private final CardService mCardService;

    public CardAssociationService(@NonNull final CardService cardService) {
        mCardService = cardService;
    }

    public MPCall<Card> associateCardToUser(@NonNull final String accessToken, @NonNull final String cardTokenId,
        @NonNull final String paymentMethodId) {
        final HashMap<String, Object> body = new HashMap<>();
        body.put("card_token_id", cardTokenId);
        final Map<String, Object> paymentMethodBody = new HashMap<>();
        paymentMethodBody.put("id", paymentMethodId);
        body.put("payment_method", paymentMethodBody);

        return mCardService.assignCard(accessToken, body);
    }
}
