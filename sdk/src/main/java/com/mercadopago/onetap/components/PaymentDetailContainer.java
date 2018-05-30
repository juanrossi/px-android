package com.mercadopago.onetap.components;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.model.Item;
import com.mercadopago.viewmodel.OneTapModel;
import javax.annotation.Nonnull;

public class PaymentDetailContainer extends CompactComponent<OneTapModel, Void> {

    public PaymentDetailContainer(@NonNull final OneTapModel oneTapModel) {
        super(oneTapModel);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        addItemDetails(parent);
        addDiscount(parent);
        return null;
    }

    private void addItemDetails(final ViewGroup parent) {
        for (Item item : props.getCheckoutPreference().getItems()) {
            parent.addView(new DetailItem(item).render(parent));
        }
    }

    private void addDiscount(final ViewGroup parent) {
        //TODO when we have discounts
        if (props.hasDiscount()) {
            //Fruta add

        }
    }
}
