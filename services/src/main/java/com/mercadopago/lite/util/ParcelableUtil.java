package com.mercadopago.lite.util;

import android.os.Parcel;
import java.math.BigDecimal;

public class ParcelableUtil {

    public static BigDecimal getBigDecimalReadByte(final Parcel in) {
        if (in.readByte() == 0) {
            return null;
        } else {
            return new BigDecimal(in.readString());
        }
    }

    public static Integer getIntegerReadByte(final Parcel in) {
        if (in.readByte() == 0) {
            return null;
        } else {
            return in.readInt();
        }
    }

    public static void writeByte(final Parcel dest, final BigDecimal number) {
        if (number == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(number.toString());
        }
    }

    public static void writeByte(final Parcel dest, final Integer number) {
        if (number == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(number);
        }
    }
}
