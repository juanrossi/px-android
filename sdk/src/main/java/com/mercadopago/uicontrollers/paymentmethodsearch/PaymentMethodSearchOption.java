package com.mercadopago.uicontrollers.paymentmethodsearch;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mercadopago.R;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.model.PaymentMethodSearchItem;
import com.mercadopago.util.ResourceUtil;

public class PaymentMethodSearchOption implements PaymentMethodSearchViewController {

    private static final int COMMENT_MAX_LENGTH = 75;

    protected PaymentMethodSearchItem mItem;
    protected Context mContext;
    protected View mView;
    protected MPTextView mDescription;
    protected MPTextView mComment;
    protected ImageView mIcon;
    protected View.OnClickListener mListener;

    public PaymentMethodSearchOption(Context context, PaymentMethodSearchItem item) {
        mContext = context;
        mItem = item;
    }

    @Override
    public View inflateInParent(ViewGroup parent, boolean attachToRoot) {
        mView = LayoutInflater.from(mContext)
                .inflate(R.layout.mpsdk_row_pm_search_item, parent, attachToRoot);
        if (mListener != null) {
            mView.setOnClickListener(mListener);
        }
        return mView;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public void initializeControls() {
        mDescription = mView.findViewById(R.id.mpsdkDescription);
        mComment = mView.findViewById(R.id.mpsdkComment);
        mIcon = mView.findViewById(R.id.mpsdkImage);

    }

    private boolean hasToShowComment(PaymentMethodSearchItem item) {
        return (!(item.getId().equals(PaymentTypes.CREDIT_CARD) ||
                item.getId().equals(PaymentTypes.DEBIT_CARD) ||
                item.getId().equals(PaymentTypes.PREPAID_CARD)));
    }

    @Override
    public void draw() {
        if (mItem.hasDescription()) {
            mDescription.setVisibility(View.VISIBLE);
            mDescription.setText(mItem.getDescription());
        } else {
            mDescription.setVisibility(View.GONE);
        }
        if (hasToShowComment(mItem) && mItem.hasComment() && mItem.getComment().length() < COMMENT_MAX_LENGTH) {
            mComment.setText(mItem.getComment());
        }

        int resourceId = 0;

        final boolean needsTint = needsTint();

        final StringBuilder imageName = new StringBuilder();
        if (!mItem.getId().isEmpty()) {
            if (needsTint) {
                imageName.append(ResourceUtil.TINT_PREFIX);
            }
            imageName.append(mItem.getId());
        }

        if (mItem.isIconRecommended()) {
            resourceId = ResourceUtil.getIconResource(mContext, imageName.toString());
        }

        if (resourceId != 0) {
            mIcon.setImageResource(resourceId);
        } else {
            mIcon.setVisibility(View.GONE);
        }

        if(needsTint) {
            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.mpsdk_main_color),
                PorterDuff.Mode.MULTIPLY);
        }
    }

    private boolean needsTint() {
        final int originalColor = ContextCompat.getColor(mContext, R.color.mpsdk_original_main_color);
        final int realColor = ContextCompat.getColor(mContext, R.color.mpsdk_main_color);
        return (originalColor != realColor) && (mItem.isGroup() || mItem.isPaymentType());
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        mListener = listener;
        if (mView != null) {
            mView.setOnClickListener(listener);
        }
    }
}
