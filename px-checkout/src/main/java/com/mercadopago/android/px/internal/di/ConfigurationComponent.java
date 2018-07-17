package com.mercadopago.android.px.internal.di;

import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;

public interface ConfigurationComponent {

    PaymentSettingRepository getPaymentSettings();
}
