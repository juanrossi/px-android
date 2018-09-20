package com.mercadopago.android.px.model;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.util.TextUtil;
import java.io.Serializable;
import java.math.BigDecimal;

public class PaymentData implements Serializable {
    private BigDecimal transactionAmount;
    private PaymentMethod paymentMethod;
    private Issuer issuer;
    private PayerCost payerCost;
    private Token token;
    private Payer payer;
    private String couponCode;

    @Nullable private Discount discount;
    @Nullable private Campaign campaign;

    @Nullable
    public Campaign getCampaign() {
        return campaign;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Issuer getIssuer() {
        return issuer;
    }

    public void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    public PayerCost getPayerCost() {
        return payerCost;
    }

    public void setPayerCost(PayerCost payerCost) {
        this.payerCost = payerCost;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void setDiscount(@Nullable final Discount discount) {
        this.discount = discount;
    }

    @Nullable
    public Discount getDiscount() {
        return discount;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public boolean containsCardInfo() {
        return getToken() != null && !TextUtil.isEmpty(getToken().getCardId());
    }

    public void setCampaign(@Nullable final Campaign campaign) {
        this.campaign = campaign;
    }
}
