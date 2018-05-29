package com.mercadopago.components;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.R;
import com.mercadopago.TermsAndConditionsActivity;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.review_and_confirm.models.TermsAndConditionsModel;
import javax.annotation.Nonnull;

public class TermsAndCondition extends CompactComponent<TermsAndConditionsModel, Void> {

    public TermsAndCondition(@NonNull final TermsAndConditionsModel props) {
        super(props);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final View root = inflate(parent, R.layout.mpsdk_view_terms_and_condition);
        final View termsAndConditionsView = root.findViewById(R.id.mpsdkCheckoutTermsAndConditions);
        termsAndConditionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                TermsAndConditionsActivity.start(termsAndConditionsView.getContext(), props.getSiteId());
            }
        });
        return root;
    }
}
