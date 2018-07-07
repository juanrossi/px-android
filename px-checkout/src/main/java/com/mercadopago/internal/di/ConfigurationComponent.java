package com.mercadopago.internal.di;

import com.mercadopago.internal.repository.PaymentSettingRepository;

public interface ConfigurationComponent {

    PaymentSettingRepository getPaymentSettings();
}
