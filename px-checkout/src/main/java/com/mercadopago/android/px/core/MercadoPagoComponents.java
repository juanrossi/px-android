package com.mercadopago.android.px.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.mercadopago.BankDealsActivity;
import com.mercadopago.CardVaultActivity;
import com.mercadopago.GuessingCardActivity;
import com.mercadopago.InstallmentsActivity;
import com.mercadopago.IssuersActivity;
import com.mercadopago.PayerInformationActivity;
import com.mercadopago.PaymentMethodsActivity;
import com.mercadopago.PaymentTypesActivity;
import com.mercadopago.PaymentVaultActivity;
import com.mercadopago.ReviewPaymentMethodsActivity;
import com.mercadopago.SecurityCodeActivity;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.paymentresult.PaymentResultActivity;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.preferences.PaymentResultScreenPreference;
import com.mercadopago.util.JsonUtil;
import java.math.BigDecimal;
import java.util.List;

public class MercadoPagoComponents {

    private MercadoPagoComponents() {
    }

    public static class Activities {

        public static final int PAYMENT_METHODS_REQUEST_CODE = 1;
        public static final int INSTALLMENTS_REQUEST_CODE = 2;
        public static final int ISSUERS_REQUEST_CODE = 3;
        public static final int PAYMENT_RESULT_REQUEST_CODE = 5;
        public static final int CALL_FOR_AUTHORIZE_REQUEST_CODE = 7;
        public static final int PENDING_REQUEST_CODE = 8;
        public static final int REJECTION_REQUEST_CODE = 9;
        public static final int PAYMENT_VAULT_REQUEST_CODE = 10;
        public static final int BANK_DEALS_REQUEST_CODE = 11;
        public static final int GUESSING_CARD_REQUEST_CODE = 13;
        public static final int INSTRUCTIONS_REQUEST_CODE = 14;
        public static final int CARD_VAULT_REQUEST_CODE = 15;
        public static final int CONGRATS_REQUEST_CODE = 16;
        public static final int PAYMENT_TYPES_REQUEST_CODE = 17;
        public static final int SECURITY_CODE_REQUEST_CODE = 18;
        public static final int REVIEW_AND_CONFIRM_REQUEST_CODE = 20;
        public static final int REVIEW_PAYMENT_METHODS_REQUEST_CODE = 21;
        public static final int PAYER_INFORMATION_REQUEST_CODE = 22;

        public static final int HOOK_1 = 50;
        public static final int HOOK_1_PLUGIN = 52;
        public static final int HOOK_2 = 60;
        public static final int HOOK_3 = 70;

        public static final int PLUGIN_PAYMENT_METHOD_REQUEST_CODE = 100;

        private Activities() {
        }

        public static class PaymentVaultActivityBuilder {

            private Activity activity;
            private String merchantPublicKey;
            private Boolean showBankDeals;
            private Integer maxSavedCards;
            private boolean installmentsReviewEnabled;
            private boolean showAllSavedCardsEnabled;
            private boolean escEnabled;

            public PaymentVaultActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public PaymentVaultActivityBuilder setShowBankDeals(Boolean showBankDeals) {
                this.showBankDeals = showBankDeals;
                return this;
            }

            public PaymentVaultActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }


            public PaymentVaultActivityBuilder setMaxSavedCards(Integer maxSavedCards) {
                this.maxSavedCards = maxSavedCards;
                return this;
            }

            public PaymentVaultActivityBuilder setShowAllSavedCardsEnabled(boolean showAll) {
                showAllSavedCardsEnabled = showAll;
                return this;
            }

            public PaymentVaultActivityBuilder setESCEnabled(boolean escEnabled) {
                this.escEnabled = escEnabled;
                return this;
            }

            public PaymentVaultActivityBuilder setInstallmentsReviewEnabled(boolean installmentsReviewEnabled) {
                this.installmentsReviewEnabled = installmentsReviewEnabled;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }

                startPaymentVaultActivity();
            }

            private void startPaymentVaultActivity() {

                final Intent intent = new Intent(activity, PaymentVaultActivity.class);
                intent.putExtra("maxSavedCards", maxSavedCards);
                intent.putExtra("showAllSavedCardsEnabled", showAllSavedCardsEnabled);
                intent.putExtra("escEnabled", escEnabled);
                intent.putExtra("merchantPublicKey", merchantPublicKey);
                intent.putExtra("showBankDeals", showBankDeals);
                intent.putExtra("installmentsReviewEnabled", installmentsReviewEnabled);
                activity.startActivityForResult(intent, PAYMENT_VAULT_REQUEST_CODE);
            }
        }

        public static class CardVaultActivityBuilder {
            private String merchantPublicKey;
            private Boolean installmentsEnabled;
            private Boolean showBankDeals;
            private Boolean escEnabled;
            private Card card;
            private PaymentRecovery paymentRecovery;
            private boolean installmentsReviewEnabled;
            private boolean automaticSelection;

            public CardVaultActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public CardVaultActivityBuilder setCard(Card card) {
                this.card = card;
                return this;
            }

            public CardVaultActivityBuilder setInstallmentsEnabled(Boolean installmentsEnabled) {
                this.installmentsEnabled = installmentsEnabled;
                return this;
            }

            public CardVaultActivityBuilder setInstallmentsReviewEnabled(boolean installmentsReviewEnabled) {
                this.installmentsReviewEnabled = installmentsReviewEnabled;
                return this;
            }

            public CardVaultActivityBuilder setShowBankDeals(Boolean showBankDeals) {
                this.showBankDeals = showBankDeals;
                return this;
            }

            public CardVaultActivityBuilder setESCEnabled(Boolean escEnabled) {
                this.escEnabled = escEnabled;
                return this;
            }

            public CardVaultActivityBuilder setPaymentRecovery(PaymentRecovery paymentRecovery) {
                this.paymentRecovery = paymentRecovery;
                return this;
            }

            public CardVaultActivityBuilder setAutomaticSelection(Boolean automaticSelection) {
                this.automaticSelection = automaticSelection;
                return this;
            }

            private Intent getIntent(final Context context) {
                Intent intent = new Intent(context, CardVaultActivity.class);
                intent.putExtra("merchantPublicKey", merchantPublicKey);
                intent.putExtra("installmentsEnabled", installmentsEnabled);
                intent.putExtra("showBankDeals", showBankDeals);
                intent.putExtra("paymentRecovery", JsonUtil.getInstance().toJson(paymentRecovery));
                intent.putExtra("card", JsonUtil.getInstance().toJson(card));
                intent.putExtra("installmentsReviewEnabled", installmentsReviewEnabled);
                intent.putExtra("automaticSelection", automaticSelection);
                intent.putExtra("escEnabled", escEnabled);
                return intent;
            }

            public void startActivity(@NonNull final Activity context, int reqCode) {
                context.startActivityForResult(getIntent(context), reqCode);
            }

            public void startActivity(final Fragment oneTapFragment, final int reqCode) {
                oneTapFragment.startActivityForResult(getIntent(oneTapFragment.getActivity()), reqCode);
            }
        }

        public static class GuessingCardActivityBuilder {
            private Activity activity;
            private String merchantPublicKey;
            private Boolean showBankDeals;
            private PaymentPreference paymentPreference;
            private Card card;
            private PaymentRecovery paymentRecovery;
            private String payerEmail;
            private String payerAccessToken;

            public GuessingCardActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public GuessingCardActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public GuessingCardActivityBuilder setPayerAccessToken(String payerAccessToken) {
                this.payerAccessToken = payerAccessToken;
                return this;
            }

            public GuessingCardActivityBuilder setCard(Card card) {
                this.card = card;
                return this;
            }

            public GuessingCardActivityBuilder setShowBankDeals(Boolean showBankDeals) {
                this.showBankDeals = showBankDeals;
                return this;
            }

            public GuessingCardActivityBuilder setPaymentRecovery(PaymentRecovery paymentRecovery) {
                this.paymentRecovery = paymentRecovery;
                return this;
            }

            public GuessingCardActivityBuilder setPaymentPreference(PaymentPreference paymentPreference) {
                this.paymentPreference = paymentPreference;
                return this;
            }

            public GuessingCardActivityBuilder setPayerEmail(String payerEmail) {
                this.payerEmail = payerEmail;
                return this;
            }

            public void startActivity() {

                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (merchantPublicKey == null && payerAccessToken == null) {
                    throw new IllegalStateException("key is null");
                }

                startGuessingCardActivity();
            }

            private void startGuessingCardActivity() {
                final Intent intent = new Intent(activity, GuessingCardActivity.class);
                intent.putExtra("merchantPublicKey", merchantPublicKey);
                intent.putExtra("showBankDeals", showBankDeals);
                intent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));
                intent.putExtra("paymentRecovery", JsonUtil.getInstance().toJson(paymentRecovery));
                intent.putExtra("card", JsonUtil.getInstance().toJson(card));
                intent.putExtra("payerEmail", payerEmail);
                intent.putExtra("payerAccessToken", payerAccessToken);
                intent.putExtra("showBankDeals", showBankDeals);
                activity.startActivityForResult(intent, GUESSING_CARD_REQUEST_CODE);
            }
        }

        public static class PaymentMethodsActivityBuilder {

            private Activity activity;
            private String merchantPublicKey;
            private PaymentPreference paymentPreference;
            private Boolean showBankDeals;

            public PaymentMethodsActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public PaymentMethodsActivityBuilder setShowBankDeals(Boolean showBankDeals) {
                this.showBankDeals = showBankDeals;
                return this;
            }

            public PaymentMethodsActivityBuilder setPaymentPreference(PaymentPreference paymentPreference) {
                this.paymentPreference = paymentPreference;
                return this;
            }

            public PaymentMethodsActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (merchantPublicKey == null) {
                    throw new IllegalStateException("public key is null");
                }
                startPaymentMethodsActivity();
            }

            private void startPaymentMethodsActivity() {
                Intent paymentMethodsIntent = new Intent(activity, PaymentMethodsActivity.class);
                paymentMethodsIntent.putExtra("merchantPublicKey", merchantPublicKey);
                paymentMethodsIntent.putExtra("showBankDeals", showBankDeals);
                paymentMethodsIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

                activity.startActivityForResult(paymentMethodsIntent, PAYMENT_METHODS_REQUEST_CODE);
            }
        }

        public static class IssuersActivityBuilder {
            private Activity activity;
            private CardInfo cardInformation;
            private String merchantPublicKey;
            private PaymentMethod paymentMethod;
            private List<Issuer> issuers;
            private String payerAccessToken;

            public IssuersActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public IssuersActivityBuilder setCardInfo(CardInfo cardInformation) {
                this.cardInformation = cardInformation;
                return this;
            }

            public IssuersActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public IssuersActivityBuilder setPaymentMethod(PaymentMethod paymentMethod) {
                this.paymentMethod = paymentMethod;
                return this;
            }

            public IssuersActivityBuilder setIssuers(List<Issuer> issuers) {
                this.issuers = issuers;
                return this;
            }

            public IssuersActivityBuilder setPayerAccessToken(String payerAccessToken) {
                this.payerAccessToken = payerAccessToken;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (merchantPublicKey == null && payerAccessToken == null) {
                    throw new IllegalStateException("key is null");
                }
                if (paymentMethod == null) {
                    throw new IllegalStateException("payment method is null");
                }
                startIssuersActivity();
            }

            private void startIssuersActivity() {
                Intent intent = new Intent(activity, IssuersActivity.class);
                intent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(paymentMethod));
                intent.putExtra("merchantPublicKey", merchantPublicKey);
                intent.putExtra("payerAccessToken", payerAccessToken);
                intent.putExtra("issuers", JsonUtil.getInstance().toJson(issuers));
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInformation));
                activity.startActivityForResult(intent, ISSUERS_REQUEST_CODE);
            }
        }

        public static class InstallmentsActivityBuilder {
            private Activity activity;
            private Site site;
            private CardInfo cardInfo;
            private String merchantPublicKey;
            private List<PayerCost> payerCosts;
            private Issuer issuer;
            private PaymentMethod paymentMethod;
            private PaymentPreference paymentPreference;
            private String payerEmail;
            private Boolean installmentsEnabled;
            private Boolean installmentsReviewEnabled;
            private String payerAccessToken;

            public InstallmentsActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public InstallmentsActivityBuilder setSite(Site site) {
                this.site = site;
                return this;
            }

            public InstallmentsActivityBuilder setCardInfo(CardInfo cardInformation) {
                cardInfo = cardInformation;
                return this;
            }

            public InstallmentsActivityBuilder setPaymentPreference(PaymentPreference paymentPreference) {
                this.paymentPreference = paymentPreference;
                return this;
            }

            public InstallmentsActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public InstallmentsActivityBuilder setPayerAccessToken(String payerAccessToken) {
                this.payerAccessToken = payerAccessToken;
                return this;
            }

            public InstallmentsActivityBuilder setPaymentMethod(PaymentMethod paymentMethod) {
                this.paymentMethod = paymentMethod;
                return this;
            }

            public InstallmentsActivityBuilder setIssuer(Issuer issuer) {
                this.issuer = issuer;
                return this;
            }

            public InstallmentsActivityBuilder setPayerCosts(List<PayerCost> payerCosts) {
                this.payerCosts = payerCosts;
                return this;
            }

            public InstallmentsActivityBuilder setPayerEmail(String payerEmail) {
                this.payerEmail = payerEmail;
                return this;
            }

            public InstallmentsActivityBuilder setInstallmentsReviewEnabled(Boolean installmentsReviewEnabled) {
                this.installmentsReviewEnabled = installmentsReviewEnabled;
                return this;
            }

            public InstallmentsActivityBuilder setInstallmentsEnabled(Boolean installmentsEnabled) {
                this.installmentsEnabled = installmentsEnabled;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (site == null) {
                    throw new IllegalStateException("site is null");
                }
                if (payerCosts == null) {
                    if (merchantPublicKey == null && payerAccessToken == null) {
                        throw new IllegalStateException("key is null");
                    }
                    if (issuer == null) {
                        throw new IllegalStateException("issuer is null");
                    }
                    if (paymentMethod == null) {
                        throw new IllegalStateException("payment method is null");
                    }
                }
                startInstallmentsActivity();
            }

            private void startInstallmentsActivity() {
                final Intent intent = new Intent(activity, InstallmentsActivity.class);
                intent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(paymentMethod));
                intent.putExtra("merchantPublicKey", merchantPublicKey);
                intent.putExtra("payerAccessToken", payerAccessToken);
                intent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
                intent.putExtra("site", JsonUtil.getInstance().toJson(site));
                intent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCosts));
                intent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInfo));
                intent.putExtra("payerEmail", payerEmail);
                intent.putExtra("installmentsEnabled", installmentsEnabled);
                intent.putExtra("installmentsReviewEnabled", installmentsReviewEnabled);
                activity.startActivityForResult(intent, INSTALLMENTS_REQUEST_CODE);
            }
        }

        public static class SecurityCodeActivityBuilder {
            private Activity activity;
            private CardInfo cardInformation;
            private String merchantPublicKey;
            private String siteId;
            private String payerAccessToken;
            private PaymentMethod paymentMethod;
            private Card card;
            private Token token;
            private boolean escEnabled;
            private PaymentRecovery paymentRecovery;
            private String reason;

            public SecurityCodeActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public SecurityCodeActivityBuilder setCardInfo(CardInfo cardInformation) {
                this.cardInformation = cardInformation;
                return this;
            }

            public SecurityCodeActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public SecurityCodeActivityBuilder setSiteId(String siteId) {
                this.siteId = siteId;
                return this;
            }

            public SecurityCodeActivityBuilder setPayerAccessToken(String payerAccessToken) {
                this.payerAccessToken = payerAccessToken;
                return this;
            }

            public SecurityCodeActivityBuilder setESCEnabled(boolean escEnabled) {
                this.escEnabled = escEnabled;
                return this;
            }

            public SecurityCodeActivityBuilder setTrackingReason(String reason) {
                this.reason = reason;
                return this;
            }

            public SecurityCodeActivityBuilder setPaymentRecovery(PaymentRecovery paymentRecovery) {
                this.paymentRecovery = paymentRecovery;
                return this;
            }

            public SecurityCodeActivityBuilder setPaymentMethod(PaymentMethod paymentMethod) {
                this.paymentMethod = paymentMethod;
                return this;
            }

            public SecurityCodeActivityBuilder setCard(Card card) {
                this.card = card;
                return this;
            }

            public SecurityCodeActivityBuilder setToken(Token token) {
                this.token = token;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (merchantPublicKey == null) {
                    throw new IllegalStateException("key is null");
                }
                if (cardInformation == null) {
                    throw new IllegalStateException("card info is null");
                }
                if (paymentMethod == null) {
                    throw new IllegalStateException("payment method is null");
                }
                if (card != null && token != null && paymentRecovery == null) {
                    throw new IllegalStateException(
                        "can't start with card and token at the same time if it's not recoverable");
                }
                if (card == null && token == null) {
                    throw new IllegalStateException("card and token can't both be null");
                }

                startSecurityCodeActivity();
            }

            private void startSecurityCodeActivity() {
                Intent intent = new Intent(activity, SecurityCodeActivity.class);
                intent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(paymentMethod));
                intent.putExtra("token", JsonUtil.getInstance().toJson(token));
                intent.putExtra("card", JsonUtil.getInstance().toJson(card));
                intent.putExtra("merchantPublicKey", merchantPublicKey);
                intent.putExtra("payerAccessToken", payerAccessToken);
                intent.putExtra("siteId", siteId);
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInformation));
                intent.putExtra("paymentRecovery", JsonUtil.getInstance().toJson(paymentRecovery));
                intent.putExtra("escEnabled", escEnabled);
                intent.putExtra("reason", reason);
                activity.startActivityForResult(intent, SECURITY_CODE_REQUEST_CODE);
            }
        }

        public static class PaymentTypesActivityBuilder {
            private Activity activity;
            private CardInfo cardInformation;
            private String merchantPublicKey;
            private List<PaymentMethod> paymentMethods;
            private List<PaymentType> paymentTypes;

            public PaymentTypesActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public PaymentTypesActivityBuilder setCardInfo(CardInfo cardInformation) {
                this.cardInformation = cardInformation;
                return this;
            }

            public PaymentTypesActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public PaymentTypesActivityBuilder setPaymentMethods(List<PaymentMethod> paymentMethods) {
                this.paymentMethods = paymentMethods;
                return this;
            }

            public PaymentTypesActivityBuilder setPaymentTypes(List<PaymentType> paymentTypes) {
                this.paymentTypes = paymentTypes;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (merchantPublicKey == null) {
                    throw new IllegalStateException("key is null");
                }
                if (paymentMethods == null) {
                    throw new IllegalStateException("payment method list is null");
                }
                if (paymentTypes == null) {
                    throw new IllegalStateException("payment types list is null");
                }

                startSecurityCodeActivity();
            }

            private void startSecurityCodeActivity() {
                Intent intent = new Intent(activity, PaymentTypesActivity.class);
                intent.putExtra("paymentMethods", JsonUtil.getInstance().toJson(paymentMethods));
                intent.putExtra("paymentTypes", JsonUtil.getInstance().toJson(paymentTypes));
                intent.putExtra("merchantPublicKey", merchantPublicKey);
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInformation));
                activity.startActivityForResult(intent, PAYMENT_TYPES_REQUEST_CODE);
            }
        }

        public static class PayerInformationActivityBuilder {
            private Activity activity;
            private String merchantPublicKey;
            private String payerAccessToken;

            public PayerInformationActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public PayerInformationActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public PayerInformationActivityBuilder setPayerAccessToken(String payerAccessToken) {
                this.payerAccessToken = payerAccessToken;
                return this;
            }

            public void startActivity() {

                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (merchantPublicKey == null) {
                    throw new IllegalStateException("key is null");
                }
                startPayerInformationActivity();
            }

            private void startPayerInformationActivity() {
                Intent payerInformationIntent = new Intent(activity, PayerInformationActivity.class);

                payerInformationIntent.putExtra("merchantPublicKey", merchantPublicKey);
                payerInformationIntent.putExtra("payerAccessToken", payerAccessToken);

                activity.startActivityForResult(payerInformationIntent, PAYER_INFORMATION_REQUEST_CODE);
            }
        }

        public static class PaymentResultActivityBuilder {
            private Activity activity;
            private String merchantPublicKey;
            private String payerAccessToken;
            private Integer congratsDisplay;
            private Discount discount;
            private PaymentResult paymentResult;
            private Site site;
            private BigDecimal amount;
            private PaymentResultScreenPreference paymentResultScreenPreference;

            public PaymentResultActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public PaymentResultActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public PaymentResultActivityBuilder setPayerAccessToken(String accessToken) {
                payerAccessToken = accessToken;
                return this;
            }

            public PaymentResultActivityBuilder setDiscount(Discount discount) {
                this.discount = discount;
                return this;
            }

            public PaymentResultActivityBuilder setCongratsDisplay(Integer congratsDisplay) {
                this.congratsDisplay = congratsDisplay;
                return this;
            }

            public PaymentResultActivityBuilder setSite(Site site) {
                this.site = site;
                return this;
            }

            public PaymentResultActivityBuilder setAmount(BigDecimal amount) {
                this.amount = amount;
                return this;
            }

            public PaymentResultActivityBuilder setPaymentResultScreenPreference(
                PaymentResultScreenPreference preference) {
                paymentResultScreenPreference = preference;
                return this;
            }

            public PaymentResultActivityBuilder setPaymentResult(PaymentResult paymentResult) {
                this.paymentResult = paymentResult;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (paymentResult == null) {
                    throw new IllegalStateException("payment result is null");
                }
                if (merchantPublicKey == null) {
                    throw new IllegalStateException("public key is null");
                }

                startPaymentResultActivity();
            }

            private void startPaymentResultActivity() {
                final Intent resultIntent = new Intent(activity, PaymentResultActivity.class);
                resultIntent.putExtra("merchantPublicKey", merchantPublicKey);
                resultIntent.putExtra("payerAccessToken", payerAccessToken);
                resultIntent.putExtra("discount", JsonUtil.getInstance().toJson(discount));
                resultIntent.putExtra("congratsDisplay", congratsDisplay);
                resultIntent.putExtra("paymentResult", JsonUtil.getInstance().toJson(paymentResult));
                resultIntent.putExtra("site", JsonUtil.getInstance().toJson(site));
                resultIntent.putExtra("paymentResultScreenPreference",
                    JsonUtil.getInstance().toJson(paymentResultScreenPreference));
                if (amount != null) {
                    resultIntent.putExtra("amount", amount.toString());
                }

                activity.startActivityForResult(resultIntent, PAYMENT_RESULT_REQUEST_CODE);
            }
        }

        public static class BankDealsActivityBuilder {

            private Activity activity;
            private String merchantPublicKey;
            private List<BankDeal> bankDeals;
            private String payerAccessToken;

            public BankDealsActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public BankDealsActivityBuilder setBankDeals(List<BankDeal> bankDeals) {
                this.bankDeals = bankDeals;
                return this;
            }

            public BankDealsActivityBuilder setMerchantPublicKey(String merchantPublicKey) {
                this.merchantPublicKey = merchantPublicKey;
                return this;
            }

            public BankDealsActivityBuilder setPayerAccessToken(String payerAccessToken) {
                this.payerAccessToken = payerAccessToken;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (merchantPublicKey == null) {
                    throw new IllegalStateException("public key is null");
                }
                startBankDealsActivity();
            }

            private void startBankDealsActivity() {
                Intent bankDealsIntent = new Intent(activity, BankDealsActivity.class);
                bankDealsIntent.putExtra("merchantPublicKey", merchantPublicKey);
                bankDealsIntent.putExtra("payerAccessToken", payerAccessToken);
                if (bankDeals != null) {
                    bankDealsIntent.putExtra("bankDeals", JsonUtil.getInstance().toJson(bankDeals));
                }
                activity.startActivityForResult(bankDealsIntent, BANK_DEALS_REQUEST_CODE);
            }
        }

        public static class ReviewPaymentMethodsActivityBuilder {

            private Activity activity;
            private List<PaymentMethod> paymentMethods;
            private String publicKey;

            public ReviewPaymentMethodsActivityBuilder setActivity(Activity activity) {
                this.activity = activity;
                return this;
            }

            public ReviewPaymentMethodsActivityBuilder setPaymentMethods(List<PaymentMethod> paymentMethods) {
                this.paymentMethods = paymentMethods;
                return this;
            }

            public ReviewPaymentMethodsActivityBuilder setPublicKey(String publicKey) {
                this.publicKey = publicKey;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (publicKey == null) {
                    throw new IllegalStateException("public key is null");
                }
                if (paymentMethods == null) {
                    throw new IllegalStateException("payment methods is null");
                }
                if (paymentMethods.isEmpty()) {
                    throw new IllegalStateException("payment methods is empty");
                }
                startReviewPaymentMethodsActivity();
            }

            private void startReviewPaymentMethodsActivity() {
                Intent intent = new Intent(activity, ReviewPaymentMethodsActivity.class);
                intent.putExtra("paymentMethods", JsonUtil.getInstance().toJson(paymentMethods));
                intent.putExtra("publicKey", publicKey);
                activity.startActivityForResult(intent, REVIEW_PAYMENT_METHODS_REQUEST_CODE);
            }
        }
    }
}
