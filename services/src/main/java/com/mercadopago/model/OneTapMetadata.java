package com.mercadopago.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class OneTapMetadata implements Parcelable, Serializable {
    @SerializedName("payment_method_id")
    public String paymentMethodId;
    @SerializedName("payment_type_id")
    public String paymentTypeId;
    public CardPaymentMetadata card;

    public OneTapMetadata(final String paymentMethodId,
        final String paymentTypeId,
        final CardPaymentMetadata card) {
        this.paymentMethodId = paymentMethodId;
        this.paymentTypeId = paymentTypeId;
        this.card = card;
    }

    protected OneTapMetadata(Parcel in) {
        paymentMethodId = in.readString();
        paymentTypeId = in.readString();
        card = in.readParcelable(CardPaymentMetadata.class.getClassLoader());
    }

    public static final Creator<OneTapMetadata> CREATOR = new Creator<OneTapMetadata>() {
        @Override
        public OneTapMetadata createFromParcel(Parcel in) {
            return new OneTapMetadata(in);
        }

        @Override
        public OneTapMetadata[] newArray(int size) {
            return new OneTapMetadata[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(paymentMethodId);
        dest.writeString(paymentTypeId);
        dest.writeParcelable(card, flags);
    }
}
