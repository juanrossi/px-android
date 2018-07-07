package com.mercadopago.android.px.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.paymentresult.components.LineSeparator;
import com.mercadopago.util.ViewUtils;
import javax.annotation.Nonnull;

public class Footer extends CompactComponent<Footer.Props, ActionDispatcher> {

    public static class Props {

        public final Button.Props buttonAction;
        public final Button.Props linkAction;

        public Props(@Nullable final Button.Props buttonAction,
            @Nullable final Button.Props linkAction) {
            this.buttonAction = buttonAction;
            this.linkAction = linkAction;
        }
    }

    public Footer(@NonNull final Props props,
        @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final Context context = parent.getContext();
        final LinearLayout linearContainer = CompactComponent.createLinearContainer(context);
        linearContainer.setBackgroundColor(context.getResources().getColor(R.color.mpsdk_white_background));
        final int padding = context.getResources().getDimensionPixelSize(R.dimen.mpsdk_s_margin);
        linearContainer.setPadding(0, 0, 0, padding);

        final LineSeparator lineSeparator = new LineSeparator(new LineSeparator.Props(R.color.mpsdk_med_light_gray));
        linearContainer.addView(lineSeparator.render(linearContainer));

        if (props.buttonAction != null) {
            final ButtonPrimary buttonPrimary =
                new ButtonPrimary(new Button.Props(props.buttonAction.label, props.buttonAction.action),
                    new Button.Actions() {
                        @Override
                        public void onClick(final Action action) {
                            if (getActions() != null) {
                                getActions().dispatch(action);
                            }
                        }
                    });
            final View buttonView = buttonPrimary.render(linearContainer);
            ViewUtils.setMarginInView(buttonView, padding, padding, padding, 0);
            linearContainer.addView(buttonView);
        }

        if (props.linkAction != null) {
            final ButtonLink buttonLink =
                new ButtonLink(new Button.Props(props.linkAction.label, props.linkAction.action), new Button.Actions() {
                    @Override
                    public void onClick(final Action action) {
                        if (getActions() != null) {
                            getActions().dispatch(action);
                        }
                    }
                });
            final View buttonLinkView = buttonLink.render(linearContainer);
            ViewUtils.setMarginInView(buttonLinkView, padding, padding, padding, 0);
            linearContainer.addView(buttonLinkView);
        }

        return linearContainer;
    }
}