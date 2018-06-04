package com.mercadopago.views;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.CouponDiscount;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Site;
import com.mercadopago.util.textformatter.TextFormatter;
import java.math.BigDecimal;

import static com.mercadopago.lite.util.CurrenciesUtil.getLocalizedAmountWithCurrencySymbol;

public class AmountView extends LinearLayoutCompat {

    @Nullable
    private OnClick callback;

    private TextView amountDescription;
    private View amountContainer;
    private TextView amountBeforeDiscount;
    private TextView maxCouponAmount;
    private TextView finalAmount;
    private View line;
    private View arrow;

    public interface OnClick {

        void onDetailClicked(@NonNull final Discount discount, @NonNull final Campaign campaign);

        void onDetailClicked(@NonNull final CouponDiscount discount, @NonNull final Campaign campaign);

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
        amountDescription = findViewById(R.id.amount_description);
        amountBeforeDiscount = findViewById(R.id.amount_before_discount);
        finalAmount = findViewById(R.id.final_amount);
        maxCouponAmount = findViewById(R.id.max_coupon_amount);
        arrow = findViewById(R.id.blue_arrow);
        amountContainer = findViewById(R.id.amount_container);
        amountBeforeDiscount.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
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
        @NonNull final Campaign campaign,
        @NonNull final BigDecimal totalAmount,
        @NonNull final Site site) {
        showDiscount(discount, campaign, totalAmount, site);
        showEffectiveAmount(totalAmount.subtract(discount.getCouponAmount()), site);
    }

    public void showCouponInput() {
        //TODO implement -> go to input
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
        configureViewsVisibilityDefault();
        amountDescription.setText(R.string.mpsdk_total_to_pay);
        amountDescription.setTextColor(getResources().getColor(R.color.mpsdk_summary_text_color));
        showEffectiveAmount(totalAmount, site);
    }

    private void configureViewsVisibilityDefault() {
        amountBeforeDiscount.setVisibility(GONE);
        maxCouponAmount.setVisibility(GONE);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) amountContainer.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
        }

        amountContainer.setLayoutParams(params);
        arrow.setVisibility(GONE);
    }

    public void show(@NonNull final CouponDiscount discount,
        @NonNull final Campaign campaign,
        @NonNull final BigDecimal totalAmount,
        @NonNull final Site site) {

        showDiscount(discount, campaign, totalAmount, site);
        showEffectiveAmount(totalAmount.subtract(discount.getCouponAmount()), site);

        configureDiscountCouponAmountDescription();
        configureDiscountCouponViewsVisibility();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (callback != null) {
                    callback.onDetailClicked(discount, campaign);
                }
            }
        });
        throw new UnsupportedOperationException();
    }

    private void configureDiscountCouponViewsVisibility() {
        maxCouponAmount.setVisibility(GONE);
        amountBeforeDiscount.setVisibility(GONE);
    }

    private void configureDiscountCouponAmountDescription() {
        amountDescription.setVisibility(VISIBLE);
        amountDescription.setText(R.string.mpsdk_has_a_coupon_discount);
        amountDescription.setTextColor(getResources().getColor(R.color.mpsdk_discount_coupon));
    }

    private void showDiscount(final @NonNull Discount discount,
        final @NonNull Campaign campaign,
        final @NonNull BigDecimal totalAmount,
        final @NonNull Site site) {
        configureDiscountAmountDescription(discount, campaign);

        configureViewsVisibilityWhenDiscount(totalAmount, site);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (callback != null) {
                    callback.onDetailClicked(discount, campaign);
                }
            }
        });
    }

    private void configureViewsVisibilityWhenDiscount(final @NonNull BigDecimal totalAmount,
        final @NonNull Site site) {
        arrow.setVisibility(VISIBLE);
        amountBeforeDiscount.setVisibility(VISIBLE);
        amountBeforeDiscount.setText(getLocalizedAmountWithCurrencySymbol(totalAmount, site));
    }

    private void configureDiscountAmountDescription(final Discount discount, final Campaign campaign) {
        amountDescription.setVisibility(VISIBLE);
        amountDescription.setTextColor(getResources().getColor(R.color.mpsdk_discount_description));
        configureDiscountOffMessage(discount);
        configureMaxCouponAmountMessage(campaign);
    }

    private void showEffectiveAmount(final @NonNull BigDecimal totalAmount, final @NonNull Site site) {
        finalAmount.setText(getLocalizedAmountWithCurrencySymbol(totalAmount, site));
    }

    private void configureMaxCouponAmountMessage(final Campaign campaign) {
        if (campaign.hasMaxCouponAmount()) {
            maxCouponAmount.setVisibility(VISIBLE);
            maxCouponAmount.setText(R.string.mpsdk_with_max_coupon_amount);
        } else {
            maxCouponAmount.setVisibility(GONE);
        }
    }

    private void configureDiscountOffMessage(final Discount discount) {
        if (discount.hasPercentOff()) {
            TextFormatter.withCurrencyId(discount.getCurrencyId())
                .noSpace().noSymbol()
                .amount(discount.getPercentOff())
                .normalDecimals()
                .into(amountDescription)
                .holder(R.string.mpsdk_discount_percent_off_percent);
        } else {
            TextFormatter.withCurrencyId(discount.getCurrencyId())
                .withSpace()
                .amount(discount.getAmountOff())
                .normalDecimals()
                .into(amountDescription)
                .holder(R.string.mpsdk_discount_amount_off);
        }
    }
}
