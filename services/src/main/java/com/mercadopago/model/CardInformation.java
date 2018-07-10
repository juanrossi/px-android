package com.mercadopago.model;

import java.io.Serializable;

public interface CardInformation extends Serializable {

    int CARD_NUMBER_MAX_LENGTH = 16;

    Integer getExpirationMonth();

    Integer getExpirationYear();

    Cardholder getCardHolder();

    String getLastFourDigits();

    String getFirstSixDigits();

    Integer getSecurityCodeLength();
}
