package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CardPaymentMetadata implements Parcelable, Serializable {

    private String id;

    @SerializedName("selected_payer_cost")
    private PayerCost payerCost;

    protected CardPaymentMetadata(Parcel in) {
        id = in.readString();
        payerCost = in.readParcelable(PayerCost.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(payerCost, flags);
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

    @NonNull
    public PayerCost getAutoSelectedInstallment() {
        return payerCost;
    }

    public String getId() {
        return id;
    }

}
