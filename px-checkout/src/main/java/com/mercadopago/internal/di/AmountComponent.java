package com.mercadopago.internal.di;

import com.mercadopago.internal.repository.AmountRepository;

public interface AmountComponent {

    AmountRepository getAmountRepository();
}
