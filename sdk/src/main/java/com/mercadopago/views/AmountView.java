package com.mercadopago.views;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.model.CouponDiscount;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Site;
import java.math.BigDecimal;

import static com.mercadopago.lite.util.CurrenciesUtil.getLocalizedAmountWithCurrencySymbol;

public class AmountView extends LinearLayoutCompat {

    @Nullable private OnClick callback;

    private TextView discountAmount;
    private TextView amount;
    private TextView discountWording;
    private TextView totalAmountLessDiscount;
    private View line;
    private View discountRow;

    public interface OnClick {

        void onDetailClicked(@NonNull final Discount discount);

        void onDetailClicked(@NonNull final CouponDiscount discount);

        void onInputRequestClicked();
    }

    public AmountView(@NonNull final Context context) {
        super(context);
        init();
    }

    public void setOnClickListener(OnClick callback) {
        this.callback = callback;
    }

    public AmountView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AmountView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.mpsdk_amount_layout, this);
        line = findViewById(R.id.line);
        discountAmount = findViewById(R.id.discount_amount);
        amount = findViewById(R.id.total_amount);
        totalAmountLessDiscount = findViewById(R.id.amount_less_discount);
        discountRow = findViewById(R.id.discount_row);
        discountWording = findViewById(R.id.discount_wording);
        amount.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        configureElevation();
    }

    private void configureElevation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(getContext().getResources().getDimension(R.dimen.mpsdk_xxs_margin));
            line.setVisibility(GONE);
        } else {
            line.setVisibility(VISIBLE);
        }
    }

    public void show(@NonNull final Discount discount,
        @NonNull final BigDecimal totalAmount,
        @NonNull final Site site) {
        discountWording.setText(R.string.mpsdk_discount);
        showDiscount(discount, totalAmount, site);
        showEffectiveAmount(totalAmount.subtract(discount.getCouponAmount()), site);
    }

    public void showCouponInput() {
        //TODO implement -> go to input
        discountWording.setText(R.string.mpsdk_has_a_discount);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (callback != null) {
                    callback.onInputRequestClicked();
                }
            }
        });
        throw new UnsupportedOperationException();
    }

    public void show(@NonNull final BigDecimal totalAmount, @NonNull final Site site) {
        discountRow.setVisibility(GONE);
        showEffectiveAmount(totalAmount, site);
    }

    public void show(@NonNull final CouponDiscount discount,
        @NonNull final BigDecimal totalAmount,
        @NonNull final Site site) {
        showDiscount(discount, totalAmount, site);
        showEffectiveAmount(totalAmount.subtract(discount.getCouponAmount()), site);
        discountWording.setText(R.string.mpsdk_discount_code);
        //TODO change wording
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (callback != null) {
                    callback.onDetailClicked(discount);
                }
            }
        });
        throw new UnsupportedOperationException();
    }

    private void showDiscount(final @NonNull Discount discount,
        final @NonNull BigDecimal totalAmount,
        final @NonNull Site site) {
        discountRow.setVisibility(VISIBLE);
        discountAmount.setText(getLocalizedAmountWithCurrencySymbol(discount.getCouponAmount(), site));
        amount.setText(getLocalizedAmountWithCurrencySymbol(totalAmount, site));
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (callback != null) {
                    callback.onDetailClicked(discount);
                }
            }
        });
    }

    private void showEffectiveAmount(final @NonNull BigDecimal totalAmount, final @NonNull Site site) {
        totalAmountLessDiscount.setText(getLocalizedAmountWithCurrencySymbol(totalAmount, site));
    }
}
