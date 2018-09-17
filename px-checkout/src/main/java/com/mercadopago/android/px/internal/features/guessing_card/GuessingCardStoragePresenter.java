package com.mercadopago.android.px.internal.features.guessing_card;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.ArrayList;
import java.util.List;

import static com.mercadopago.android.px.internal.util.ApiUtil.RequestOrigin.GET_CARD_PAYMENT_METHODS;

public class GuessingCardStoragePresenter extends GuessingCardPresenter {

    private final String mAccessToken;
    private PaymentMethod currentPaymentMethod;

    public GuessingCardStoragePresenter(final String accessToken) {
        super();
        mAccessToken = accessToken;
    }

    @Override
    public void initialize() {
        getView().onValidStart();
        getView().hideBankDeals();
        initializeCardToken();
        getPaymentMethods();
    }

    @Nullable
    @Override
    public String getPaymentTypeId() {
        if (currentPaymentMethod != null) {
            return currentPaymentMethod.getPaymentTypeId();
        }
        return null;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return currentPaymentMethod;
    }

    @Override
    public void setPaymentMethod(@Nullable final PaymentMethod paymentMethod) {
        currentPaymentMethod = paymentMethod;
        if (paymentMethod == null) {
            clearCardSettings();
        }
    }

    @Override
    public void getIdentificationTypesAsync() {
        getResourcesProvider().getIdentificationTypesAsync(mAccessToken,
            new TaggedCallback<List<IdentificationType>>(ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES) {
                @Override
                public void onSuccess(final List<IdentificationType> identificationTypes) {
                    resolveIdentificationTypes(identificationTypes);
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().showError(error, ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES);
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                getIdentificationTypesAsync();
                            }
                        });
                    }
                }
            });
    }

    @Override
    public void getPaymentMethods() {
        getView().showProgress();
        getResourcesProvider()
            .getCardPaymentMethods(mAccessToken, new TaggedCallback<List<PaymentMethod>>(GET_CARD_PAYMENT_METHODS) {
                @Override
                public void onSuccess(final List<PaymentMethod> paymentMethods) {
                    if (isViewAttached()) {
                        getView().hideProgress();
                        mPaymentMethodGuessingController = new
                            PaymentMethodGuessingController(paymentMethods, null, null);
                        startGuessingForm();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    getView().hideProgress();
//                    setFailureRecovery(new FailureRecovery() {
//                        @Override
//                        public void recover() {
//                            getPaymentMethods();
//                        }
//                    });
//                    getView().showError(error, ApiUtil.RequestOrigin.GET_CARD_PAYMENT_METHODS);

                    List<PaymentMethod> paymentMethods = new ArrayList<>();
                    final String resource_visa =
                        "{\"id\":\"visa\",\"name\":\"Visa\",\"payment_type_id\":\"credit_card\",\"status\":\"active\",\"secure_thumbnail\":\"https://www.mercadopago.com/org-img/MP3/API/logos/visa.gif\",\"thumbnail\":\"http://img.mlstatic.com/org-img/MP3/API/logos/visa.gif\",\"deferred_capture\":\"supported\",\"settings\":[{\"bin\":{\"pattern\":\"^4\",\"exclusion_pattern\":\"^(451766|451772|405896|473711|451769|451765|451757|451764|439818|451377|451761|406290|499859|451751|489412|477053|446344|473721)\",\"installments_pattern\":\"^4\"},\"card_number\":{\"length\":16,\"validation\":\"standard\"},\"security_code\":{\"mode\":\"mandatory\",\"length\":3,\"card_location\":\"back\"}}],\"additional_info_needed\":[\"cardholder_name\",\"cardholder_identification_type\",\"cardholder_identification_number\"],\"min_allowed_amount\":0,\"max_allowed_amount\":250000,\"accreditation_time\":2880,\"financial_institutions\":[]}";
                    final String resource_master =
                        "{\"id\":\"master\",\"name\":\"Mastercard\",\"payment_type_id\":\"credit_card\",\"status\":\"active\",\"secure_thumbnail\":\"https://www.mercadopago.com/org-img/MP3/API/logos/master.gif\",\"thumbnail\":\"http://img.mlstatic.com/org-img/MP3/API/logos/master.gif\",\"deferred_capture\":\"supported\",\"settings\":[{\"card_number\":{\"validation\":\"standard\",\"length\":16},\"bin\":{\"pattern\":\"^(5|(2(221|222|223|224|225|226|227|228|229|23|24|25|26|27|28|29|3|4|5|6|70|71|720)))\",\"installments_pattern\":\"^(5|(2(221|222|223|224|225|226|227|228|229|23|24|25|26|27|28|29|3|4|5|6|70|71|720)))\",\"exclusion_pattern\":\"^(589657|589562|557039|522135|522137|527555|542702|544764|550073|528824|511849|551238|501105|501020|501021|501023|501062|501038|501057|588729|501041|501056|501075|501080|501081)\"},\"security_code\":{\"length\":3,\"card_location\":\"back\",\"mode\":\"mandatory\"}}],\"additional_info_needed\":[\"cardholder_identification_type\",\"cardholder_name\",\"cardholder_identification_number\",\"issuer_id\"],\"min_allowed_amount\":0,\"max_allowed_amount\":250000,\"accreditation_time\":2880,\"financial_institutions\":[]}";
                    paymentMethods.add(JsonUtil.getInstance().fromJson(resource_visa, PaymentMethod.class));
                    paymentMethods.add(JsonUtil.getInstance().fromJson(resource_master, PaymentMethod.class));
                    mPaymentMethodGuessingController = new
                        PaymentMethodGuessingController(paymentMethods, null, null);
                    startGuessingForm();
                }
            });
    }

    @Override
    public void onPaymentMethodSet(final PaymentMethod paymentMethod) {
        setPaymentMethod(paymentMethod);
        configureWithSettings(paymentMethod);
        loadIdentificationTypes(paymentMethod);
        getView().setPaymentMethod(paymentMethod);
        getView().resolvePaymentMethodSet(paymentMethod);
    }

    @Override
    public void resolveTokenRequest(final Token token) {
        //TODO: implement
    }

    @Nullable
    @Override
    public List<BankDeal> getBankDealsList() {
        return null;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState, final String cardSideState, final boolean lowResActive) {
        //TODO: implement
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        //TODO: implement
    }
}
