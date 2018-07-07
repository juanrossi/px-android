package com.mercadopago.testcheckout.input;


public class Amex extends FakeCard {

    public Amex(final CardState cardState, final Country country) {
        super("1234", cardState, getNumber(country));
    }

    private static String getNumber(Country country) {
        switch (country) {
            case ARGENTINA:
                return "371180303257522";
            case BRASIL:
                return "375365153556885";
            case CHILE:
                return "375778174461804";
            case COLOMBIA:
                return "374378187755283";
            default:
                return null;
        }
    }
}
