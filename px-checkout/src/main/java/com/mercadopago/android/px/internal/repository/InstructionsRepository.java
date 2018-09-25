package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.PaymentResult;
import java.util.List;

public interface InstructionsRepository {

    MPCall<List<Instruction>> getInstructions(@NonNull final PaymentResult paymentResult);
}
