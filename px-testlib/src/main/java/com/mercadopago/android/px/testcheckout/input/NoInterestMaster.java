package com.mercadopago.android.px.testcheckout.input;

import javax.annotation.Nullable;

public class NoInterestMaster extends FakeCard {

    public NoInterestMaster(final FakeCard.CardState cardState, final Country country) {
        super(cardState, getNumber(country));
    }

    @Nullable
    private static String getNumber(final Country country) {
        switch (country) {
        case ARGENTINA:
            return "5156883002652543";
        default:
            return null;
        }
    }
}
