package com.mercadopago.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class CardPaymentMetadata implements Parcelable, Serializable {

    private String id;

    private String description;

    private Issuer issuer;

    @SerializedName("last_four_digits")
    private String lastFourDigits;

    private int installments;

    @SerializedName("payer_costs")
    private List<PayerCost> payerCosts;

    protected CardPaymentMetadata(Parcel in) {
        id = in.readString();
        description = in.readString();
        issuer = in.readParcelable(Issuer.class.getClassLoader());
        lastFourDigits = in.readString();
        installments = in.readInt();
        payerCosts = in.createTypedArrayList(PayerCost.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeParcelable(issuer, flags);
        dest.writeString(lastFourDigits);
        dest.writeInt(installments);
        dest.writeTypedList(payerCosts);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CardPaymentMetadata> CREATOR = new Creator<CardPaymentMetadata>() {
        @Override
        public CardPaymentMetadata createFromParcel(Parcel in) {
            return new CardPaymentMetadata(in);
        }

        @Override
        public CardPaymentMetadata[] newArray(int size) {
            return new CardPaymentMetadata[size];
        }
    };

    @Nullable
    public PayerCost getAutoSelectedInstallment() {
        for (PayerCost payerCost : payerCosts) {
            if (payerCost.getInstallments() == installments) {
                return payerCost;
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Issuer getIssuer() {
        return issuer;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public int getInstallments() {
        return installments;
    }
}
