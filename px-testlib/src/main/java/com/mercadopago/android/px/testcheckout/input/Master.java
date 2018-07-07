package com.mercadopago.android.px.testcheckout.input;

import javax.annotation.Nullable;

public class Master extends FakeCard {

    public Master(final CardState cardState, final Country country) {
        super(cardState, getNumber(country));
    }

    @Nullable
    private static String getNumber(final Country country) {
        switch (country) {
            case ARGENTINA:
                return "5031755734530604";
            case BRASIL:
                return "5031433215406351";
            case CHILE:
                return "5416752602582580";
            case COLOMBIA:
                return "5254133674403564";
            case MEXICO:
                return "5474925432670366";
            case URUGUAY:
                return "5808887774641586";
            case VENEZUELA:
                return "5177076164300010";
            default:
                return null;
        }
    }

}
