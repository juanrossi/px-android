package com.mercadopago.model;

import java.io.Serializable;

public class PaymentType implements Serializable {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PaymentType() {
    }

    public PaymentType(String paymentTypeId) {
        id = paymentTypeId;
    }
}
