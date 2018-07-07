package com.mercadopago.android.px.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import com.mercadopago.android.px.uicontrollers.FontCache;

public class MPButton extends AppCompatButton {

    public MPButton(Context context) {
        super(context);
        init();
    }

    public MPButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MPButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = getCustomTypeface();

            if (tf != null) {
                setTypeface(tf);
            }
        }
    }

    private Typeface getCustomTypeface() {
        return FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT);
    }
}
