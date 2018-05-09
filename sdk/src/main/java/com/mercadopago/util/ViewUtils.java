package com.mercadopago.util;

import android.widget.ImageView;
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

    public static void loadOrCallError(final String imgUrl, final ImageView logo, Callback.EmptyCallback callback) {
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

}
