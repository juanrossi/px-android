package com.mercadopago.android.px.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class ExternalFragment implements Parcelable {

    @NonNull
    public final String zClassName;
    @Nullable
    public final Bundle args;

    public ExternalFragment(@NonNull Class<? extends Fragment> zClass, @Nullable final Bundle args) {
        this.zClassName = zClass.getName();
        this.args = args;
    }

    protected ExternalFragment(Parcel in) {
        zClassName = in.readString();
        args = in.readBundle(getClass().getClassLoader());
    }

    public static final Creator<ExternalFragment> CREATOR = new Creator<ExternalFragment>() {
        @Override
        public ExternalFragment createFromParcel(Parcel in) {
            return new ExternalFragment(in);
        }

        @Override
        public ExternalFragment[] newArray(int size) {
            return new ExternalFragment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(zClassName);
        dest.writeBundle(args);
    }
}
