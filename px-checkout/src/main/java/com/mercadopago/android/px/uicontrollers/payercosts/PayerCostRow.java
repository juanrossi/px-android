package com.mercadopago.android.px.uicontrollers.payercosts;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.customviews.MPTextView;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.services.util.CurrenciesUtil;
import com.mercadopago.android.px.util.InstallmentsUtil;
import java.math.BigDecimal;

/**
 * Created by mreverter on 12/5/16.
 */
public class PayerCostRow {

    private final Site mSite;

    private final String mCurrencyId;

    private final Context mContext;
    private View mView;
    private MPTextView mInstallmentsTextView;
    private MPTextView mZeroRateText;
    private MPTextView mTotalText;

    public PayerCostRow(Context context, Site site) {
        mContext = context;
        mCurrencyId = site.getCurrencyId();
        mSite = site;
    }

    public void drawPayerCost(final BigDecimal installmentsRate,
        final Integer installments,
        final BigDecimal totalAmount,
        final BigDecimal installmentAmount) {
        setInstallmentsText(installments, installmentAmount);
        if (!InstallmentsUtil.shouldWarnAboutBankInterests(mSite.getId())) {
            if (installmentsRate.compareTo(BigDecimal.ZERO) == 0) {
                mTotalText.setVisibility(View.GONE);
                if (installments > 1) {
                    mZeroRateText.setVisibility(View.VISIBLE);
                }
            } else {
                setAmountWithRateText(totalAmount);
            }
        }
    }

    public void setSmallTextSize() {
        mInstallmentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            mContext.getResources().getDimension(R.dimen.px_payer_cost_small_text));
        mZeroRateText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            mContext.getResources().getDimension(R.dimen.px_payer_cost_total_small_text));
        mTotalText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            mContext.getResources().getDimension(R.dimen.px_payer_cost_total_small_text));
    }

    private void setAmountWithRateText(BigDecimal totalAmount) {
        mTotalText.setVisibility(View.VISIBLE);
        final Spanned spannedInstallmentsText =
            CurrenciesUtil.getSpannedAmountWithCurrencySymbol(totalAmount, mCurrencyId);
        mTotalText.setText(TextUtils.concat("(", spannedInstallmentsText, ")"));
    }

    private void setInstallmentsText(Integer installments, BigDecimal installmentsAmount) {
        final Spanned spannedInstallmentsText =
            CurrenciesUtil.getSpannedAmountWithCurrencySymbol(installmentsAmount, mCurrencyId);
        final String x = mInstallmentsTextView.getContext().getString(R.string.px_installments_by);
        mInstallmentsTextView
            .setText(new SpannableStringBuilder(installments.toString()).append(x).append(" ")
                .append(spannedInstallmentsText));
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mView.setOnClickListener(listener);
    }

    public void initializeControls() {
        mInstallmentsTextView = mView.findViewById(R.id.mpsdkInstallmentsText);
        mZeroRateText = mView.findViewById(R.id.mpsdkInstallmentsZeroRate);
        mTotalText = mView.findViewById(R.id.mpsdkInstallmentsWithRate);
    }

    public View inflateInParent(ViewGroup parent, boolean attachToRoot) {
        mView = LayoutInflater.from(mContext)
            .inflate(R.layout.px_row_payer_cost_list, parent, attachToRoot);
        return mView;
    }

    public View getView() {
        return mView;
    }
}