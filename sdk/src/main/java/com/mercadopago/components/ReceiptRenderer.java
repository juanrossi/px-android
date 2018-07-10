package com.mercadopago.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewGroup;

import com.mercadopago.R;
import com.mercadopago.customviews.MPTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReceiptRenderer extends Renderer<Receipt> {

    @Override
    public View render(@NonNull final Receipt component, @NonNull final Context context, final ViewGroup parent) {
        final View receiptView = inflate(R.layout.mpsdk_payment_receipt_component, parent);
        final MPTextView descriptionTextView = receiptView.findViewById(R.id.mpsdkReceiptDescription);
        final MPTextView dateTextView = receiptView.findViewById(R.id.mpsdkReceiptDate);

        final String divider = context.getResources().getString(R.string.mpsdk_date_divider);
        final Calendar calendar = Calendar.getInstance();
        final Locale locale = context.getResources().getConfiguration().locale;

        final String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        final String month = new SimpleDateFormat("MMMM", locale).format(calendar.getTime());
        final String year = String.valueOf(calendar.get(Calendar.YEAR));

        final StringBuilder builder = new StringBuilder()
                .append(day).append(" ").append(divider).append(" ").append(month).append(" ")
                .append(divider).append(" ").append(year);

        setText(dateTextView, builder.toString());
        setText(descriptionTextView, getReceiptDescription(context, component.props.receiptId));

        return receiptView;
    }

    @VisibleForTesting
    String getReceiptDescription(@NonNull final Context context, @Nullable final String receiptId) {
        if (receiptId != null) {
            return context.getString(R.string.mpsdk_receipt, String.valueOf(receiptId));
        }
        return "";
    }
}