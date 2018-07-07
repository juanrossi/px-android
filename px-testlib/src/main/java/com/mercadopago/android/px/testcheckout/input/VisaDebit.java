package com.mercadopago.android.px.testcheckout.input;


public class VisaDebit extends FakeCard {

    public VisaDebit(final CardState cardState, final Country country) {
        super(cardState, getNumber(country));
    }

    private static String getNumber(Country country) {
        switch (country) {
            case ARGENTINA:
                return "4002768694395619";
            default:
                return null;
        }
    }
}
