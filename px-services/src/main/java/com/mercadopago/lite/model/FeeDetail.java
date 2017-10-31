package com.mercadopago.lite.model;
import java.math.BigDecimal;

/**
 * Created by mromar on 10/20/17.
 */

public class FeeDetail {

    private BigDecimal amount;
    private String feePayer;
    private String type;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getFeePayer() {
        return feePayer;
    }

    public void setFeePayer(String feePayer) {
        this.feePayer = feePayer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
