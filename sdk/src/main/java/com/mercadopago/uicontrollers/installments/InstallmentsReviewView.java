package com.mercadopago.uicontrollers.installments;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.R;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.lite.util.CurrenciesUtil;
import com.mercadopago.model.PayerCost;

/**
 * Created by mromar on 2/3/17.
 */

public class InstallmentsReviewView implements InstallmentsView {

    //Local vars
    private final Context mContext;
    private final PayerCost mPayerCost;
    private final String mCurrencyId;

    //Views
    private View mView;
    private MPTextView mInstallmentsAmount;
    private MPTextView mTotalAmount;
    private MPTextView mCftpercent;
    private AppCompatButton mInstallmentsContinueButton;

    public InstallmentsReviewView(Context context, PayerCost payerCost, String currencyId) {
        mContext = context;
        mPayerCost = payerCost;
        mCurrencyId = currencyId;
    }

    @Override
    public void draw() {
        setInstallmentAmountText();
        setTotalAmountWithRateText();
        setCFTPercentText();
    }

    private void setInstallmentAmountText() {
        String installments = mPayerCost.getInstallments().toString();
        final String x = mContext.getString(R.string.mpsdk_installments_by);
        final Spanned spannedInstallmentsText = CurrenciesUtil.getSpannedAmountWithCurrencySymbol(mPayerCost.getInstallmentAmount(), mCurrencyId);

        mInstallmentsAmount
            .setText(new SpannableStringBuilder(installments).append(x).append(" ").append(spannedInstallmentsText));
    }

    private void setTotalAmountWithRateText() {
        final Spanned spannedInstallmentsText = CurrenciesUtil.getSpannedAmountWithCurrencySymbol(mPayerCost.getTotalAmount(), mCurrencyId);
        mTotalAmount.setText(TextUtils.concat("(", spannedInstallmentsText, ")"));
    }

    private void setCFTPercentText() {
        String cftPercent = mContext.getString(R.string.mpsdk_installments_cft, mPayerCost.getCFTPercent());
        mCftpercent.setText(cftPercent);
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        mInstallmentsContinueButton.setOnClickListener(listener);
    }

    @Override
    public void initializeControls() {
        mInstallmentsContinueButton = mView.findViewById(R.id.mpsdkInstallmentsContinueButton);
        mInstallmentsAmount = mView.findViewById(R.id.mpsdkInstallmentsAmount);
        mTotalAmount = mView.findViewById(R.id.mpsdkReviewTotalAmount);
        mCftpercent = mView.findViewById(R.id.mpsdkCftpercent);
    }

    @Override
    public View inflateInParent(ViewGroup parent, boolean attachToRoot) {
        parent.removeAllViews();

        mView = LayoutInflater.from(mContext).inflate(R.layout.mpsdk_installments_review_view, parent, attachToRoot);

        return mView;
    }

    @Override
    public View getView() {
        return mView;
    }
}


