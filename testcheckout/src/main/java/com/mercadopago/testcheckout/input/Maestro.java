package com.mercadopago.testcheckout.input;


public class Maestro extends FakeCard {

    public Maestro(final CardState cardState, final Country country) {
        super(cardState, getNumber(country));
    }

    private static String getNumber(Country country) {
        switch (country) {
            case ARGENTINA:
                return "501041415281778603";
            default:
                return null;
        }
    }
}
