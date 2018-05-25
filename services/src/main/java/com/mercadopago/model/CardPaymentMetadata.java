package com.mercadopago.model;

public class CardPaymentMetadata {

    public String id;
    public String description;
    public Issuer issuer;
    public String lastFourDigits;
    public int installments;
    public PayerCost payerCost;
}
