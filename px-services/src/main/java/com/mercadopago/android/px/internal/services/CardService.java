package com.mercadopago.android.px.internal.services;

import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.Card;
import java.util.HashMap;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CardService {
    @POST("/{version}/px_mobile_api/card-association")
    MPCall<Card> assignCard(@Path(value = "version", encoded = true) String version,
        @Query("access_token") String accessToken, @Body HashMap<String, Object> body);
}
