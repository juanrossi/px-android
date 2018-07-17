package com.mercadopago.android.px.utils;

import android.support.v4.util.Pair;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import java.util.Collection;

final class DiscountSamples {

    private static final String PK_WITH_DIRECT_DISCOUNT = "TEST-93c0061e-ba7d-479c-9d52-c60b0af58a91";
    private static final String PREF_WITH_DIRECT_DISCOUNT = "241261700-459d4126-903c-4bad-bc05-82e5f13fa7d3";

    private static final String PK_WITH_CODE_DISCOUNT = "TEST-85a140f5-10ce-4d3a-ba7a-a743e066840f";
    private static final String PREF_WITH_CODE_DISCOUNT = "241261708-cd353b1b-940f-493b-b960-10106a24203c";

    private DiscountSamples() {
    }

    public static void addAll(final Collection<Pair<String, MercadoPagoCheckout.Builder>> options) {
        options.add(
            new Pair<>("Direct discount", getMercadoPagoBuilder(PK_WITH_DIRECT_DISCOUNT, PREF_WITH_DIRECT_DISCOUNT)));
        options.add(new Pair<>("Code discount", getMercadoPagoBuilder(PK_WITH_CODE_DISCOUNT, PREF_WITH_CODE_DISCOUNT)));
    }

    private static MercadoPagoCheckout.Builder getMercadoPagoBuilder(final String publicKey, final String prefId) {
        return new MercadoPagoCheckout.Builder(publicKey, prefId);
    }
}
