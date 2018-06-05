package com.mercadopago.util;

import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static android.view.View.GONE;

public final class ViewUtils {

    private ViewUtils() {
    }

    public static void loadOrGone(final String imgUrl, final ImageView logo) {
        if (!TextUtil.isEmpty(imgUrl)) {
            Picasso.with(logo.getContext())
                    .load(imgUrl)
                    .into(logo, new Callback.EmptyCallback() {
                        @Override
                        public void onError() {
                            logo.setVisibility(GONE);
                        }
                    });
        } else {
            logo.setVisibility(GONE);
        }
    }

    public static void loadOrCallError(final String imgUrl, final ImageView logo, Callback callback) {
        if (!TextUtil.isEmpty(imgUrl)) {
            Picasso.with(logo.getContext())
                    .load(imgUrl)
                    .into(logo, callback);
        } else {
            callback.onError();
        }
    }

    public static void loadOrGone(final CharSequence text, final TextView textView) {
        if (TextUtil.isEmpty(text)) {
            textView.setVisibility(GONE);
        } else {
            textView.setText(text);
        }
    }

    public static void showOrGone(final View view, final boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : GONE);
    }

    public static void setMarginBottomInView(@NonNull final View view, final int marginBottom) {
        setMarginInView(view, 0, 0, 0, marginBottom);
    }

    public static void setMarginTopInView(@NonNull final View view, final int marginTop) {
        setMarginInView(view, 0, marginTop, 0, 0);
    }


    private static void setMarginInView(@NonNull final View button, final int leftMargin, final int topMargin,
        final int rightMargin, final int bottomMargin) {
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        button.setLayoutParams(params);
    }
}
