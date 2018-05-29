package com.mercadopago.onetap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.R;
import com.mercadopago.core.CheckoutStore;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.model.Card;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Token;
import com.mercadopago.onetap.components.OneTapContainer;
import com.mercadopago.plugins.model.PaymentMethodInfo;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.viewmodel.CardPaymentModel;
import com.mercadopago.viewmodel.OneTapModel;

import static android.app.Activity.RESULT_OK;

public class OneTapFragment extends Fragment implements OneTap.View {

    private static final String ARG_ONE_TAP_MODEL = "arg_onetap_model";
    private static final String ARG_PUBLIC_KEY = "arg_public_key";
    private static final String ARG_PRIVATE_KEY = "arg_private_key";
    private static final int REQ_CODE_CARD_VAULT = 0x999;
    //TODO move to CardValueActivity
    private static final String BUNDLE_TOKEN = "token";

    private CallBack callback;
    private String privateKey;
    private String publicKey;
    private OneTapPresenter presenter;

    public static OneTapFragment getInstance(@NonNull final String publicKey,
        @Nullable final String privateKey,
        @NonNull final OneTapModel oneTapModel) {
        OneTapFragment oneTapFragment = new OneTapFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PUBLIC_KEY, publicKey);
        bundle.putString(ARG_PRIVATE_KEY, privateKey);
        bundle.putSerializable(ARG_ONE_TAP_MODEL, oneTapModel);
        oneTapFragment.setArguments(bundle);
        return oneTapFragment;
    }

    public interface CallBack {

        void onOneTapCanceled();

        void onChangePaymentMethod();

        void onOneTapPay(@NonNull final PaymentMethod paymentMethod);

        void onOneTapPay(@NonNull final CardPaymentModel cardPaymentModel);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof CallBack) {
            this.callback = (CallBack) context;
        }
    }

    @Override
    public void onDetach() {
        callback = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
        @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mpsdk_onetap_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            privateKey = arguments.getString(ARG_PRIVATE_KEY);
            publicKey = arguments.getString(ARG_PUBLIC_KEY);
            OneTapModel model = (OneTapModel) arguments.getSerializable(ARG_ONE_TAP_MODEL);
            presenter = new OneTapPresenter(model);
            configureView(view, presenter, model);
            presenter.attachView(this);
        } else {
            cancel();
        }
    }

    @Override
    public void cancel() {
        if (callback != null) {
            callback.onOneTapCanceled();
        }
    }

    private void configureView(final View view, final OneTap.Actions actions,
        final OneTapModel model) {
        ViewGroup container = view.findViewById(R.id.main_container);
        new OneTapContainer(model, actions).render(container);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQ_CODE_CARD_VAULT && resultCode == RESULT_OK) {
            //TODO change serializable output at least
            String tokenString = data.getStringExtra(BUNDLE_TOKEN);
            Token token = JsonUtil.getInstance().fromJson(tokenString, Token.class);
            presenter.onReceived(token);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void changePaymentMethod() {
        if (callback != null) {
            callback.onChangePaymentMethod();
        }
    }

    @Override
    public void showPaymentFlow(@NonNull final PaymentMethod paymentMethod) {
        if (callback != null) {
            callback.onOneTapPay(paymentMethod);
        }
    }

    @Override
    public void showPaymentFlow(@NonNull final CardPaymentModel cardPaymentModel) {
        if (callback != null) {
            callback.onOneTapPay(cardPaymentModel);
        }
    }

    @Override
    public void showPaymentFlowPlugin(@NonNull final String paymentTypeId, @NonNull final String paymentMethodId) {
        //TODO refactor - horrible way to get it but depends on context
        if (callback != null && getActivity() != null) {
            PaymentMethodInfo pluginInfo =
                CheckoutStore.getInstance().getPaymentMethodPluginInfoById(paymentMethodId, getActivity());
            callback.onOneTapPay(new PaymentMethod(pluginInfo.id, pluginInfo.name, paymentTypeId));
        }
    }

    @Override
    public void showMoreAmount() {
        // TODO
        PaymentDetailInfoDialog paymentDetailInfoDialog = new PaymentDetailInfoDialog();
        paymentDetailInfoDialog.show(getChildFragmentManager(), "");
    }

    @Override
    public void showCardFlow(@NonNull OneTapModel model, @NonNull final Card card) {
        new MercadoPagoComponents.Activities.CardVaultActivityBuilder()
            .setMerchantPublicKey(publicKey)
            .setPayerAccessToken(privateKey)
            .setCheckoutPreference(model.getCheckoutPreference())
            .setDiscount(model.getDiscount())
            .setESCEnabled(model.isEscEnabled())
            .setInstallmentsEnabled(false)
            .setCard(card)
            .startActivity(this, REQ_CODE_CARD_VAULT);
    }
}
