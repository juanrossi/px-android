package com.mercadopago.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class CardPaymentMetadata implements Parcelable, Serializable {

    public String id;

    public String description;

    public Issuer issuer;

    @SerializedName("last_four_digits")
    public String lastFourDigits;

    public int installments;

    @SerializedName("payer_costs")
    public List<PayerCost> payerCosts;

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
}
