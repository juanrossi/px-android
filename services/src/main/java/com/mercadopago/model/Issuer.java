package com.mercadopago.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.List;

public class Issuer implements Serializable, Parcelable {

    private Long id;
    private String name;

    private List<String> labels;

    public Issuer(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    protected Issuer(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        labels = in.createStringArrayList();
    }

    public static final Creator<Issuer> CREATOR = new Creator<Issuer>() {
        @Override
        public Issuer createFromParcel(Parcel in) {
            return new Issuer(in);
        }

        @Override
        public Issuer[] newArray(int size) {
            return new Issuer[size];
        }
    };

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(final List<String> labels) {
        this.labels = labels;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(name);
        dest.writeStringList(labels);
    }
}
