package com.mercadopago.android.px.internal.services;

import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.Card;
import java.util.HashMap;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CardService {
    @POST("/beta/card_association")
    MPCall<Card> assignCard(@Query("access_token") String accessToken, @Body HashMap<String, Object> body);
}
