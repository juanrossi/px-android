package com.mercadopago.onetap;

import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.R;
import com.mercadopago.components.Action;
import com.mercadopago.components.Button;
import com.mercadopago.components.ButtonPrimary;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.components.TermsAndCondition;
import com.mercadopago.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.viewmodel.OneTapModel;
import javax.annotation.Nonnull;

class OneTapContainer extends CompactComponent<OneTapModel, OneTap.Actions> {

    /* default */ OneTapContainer(final OneTapModel oneTapModel, final OneTap.Actions callBack) {
        super(oneTapModel, callBack);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        addItem(parent);
        addAmount(parent);
        addPaymentMethod(parent);
        addTermsAndConditions(parent);
        addConfirmButton(parent);
        return parent;
    }

    private void addItem(final ViewGroup parent) {
        final String defaultMultipleTitle = parent.getContext().getString(R.string.mpsdk_review_summary_products);
        final int icon =
            props.getCollectorIcon() == null ? R.drawable.mpsdk_review_item_default : props.getCollectorIcon();
        final String itemsTitle = com.mercadopago.model.Item
            .getItemsTitle(props.getCheckoutPreference().getItems(), defaultMultipleTitle);
        final View render = new Item(new Item.Props(icon, itemsTitle)).render(parent);
        parent.addView(render);
    }

    private void addAmount(final ViewGroup parent) {
        final CompactAmount.Props props = CompactAmount.Props.from(this.props);
        final View view = new CompactAmount(props, getActions())
            .render(parent);
        parent.addView(view);
    }

    private void addPaymentMethod(final ViewGroup parent) {
        final View view =
            new CompactPaymentMethod(CompactPaymentMethod.Props.createFrom(props),
                getActions()).render(parent);
        parent.addView(view);
    }

    private void addTermsAndConditions(final ViewGroup parent) {
        //TODO remove true and add depending on ...discount? login? if discount is other view that does not exists yet.
        final TermsAndConditionsModel model =
            new TermsAndConditionsModel(props.getCheckoutPreference().getSiteId(), true);
        final View view = new TermsAndCondition(model)
            .render(parent);
        parent.addView(view);
    }

    private void addConfirmButton(final @Nonnull ViewGroup parent) {
        final String confirm = parent.getContext().getString(R.string.mpsdk_pay);
        final Button.Actions actions = new Button.Actions() {
            @Override
            public void onClick(final Action action) {
                getActions().confirmPayment();
            }
        };
        final Button button = new ButtonPrimary(new Button.Props(confirm), actions);
        parent.addView(button.render(parent));
    }
}
