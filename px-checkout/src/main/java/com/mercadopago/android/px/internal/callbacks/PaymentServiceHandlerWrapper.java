package com.mercadopago.android.px.internal.callbacks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.repository.EscManager;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

public final class PaymentServiceHandlerWrapper implements PaymentServiceHandler {

    @Nullable private WeakReference<PaymentServiceHandler> handler;
    @NonNull private final PaymentRepository paymentRepository;
    @NonNull private final EscManager escManager;
    @NonNull private final Queue<Message> messages;

    public PaymentServiceHandlerWrapper(
        @NonNull final PaymentRepository paymentRepository,
        @NonNull final EscManager escManager) {
        this.paymentRepository = paymentRepository;
        this.escManager = escManager;
        messages = new LinkedList<>();
    }

    public void setHandler(@Nullable final PaymentServiceHandler handler) {
        if (handler == null) {
            this.handler = null;
        } else {
            this.handler = new WeakReference<>(handler);
        }
    }

    @Override
    public void onCvvRequired(@NonNull final Card card) {
        addAndProcess(new CVVRequiredMessage(card));
    }

    @Override
    public void onVisualPayment() {
        addAndProcess(new VisualPaymentMessage());
    }

    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        addAndProcess(new RecoverPaymentEscInvalidMessage(recovery));
    }

    @Override
    public void onPaymentFinished(@NonNull final Payment payment) {
        if (handleEsc(payment)) {
            onRecoverPaymentEscInvalid(paymentRepository.createRecoveryForInvalidESC());
        } else {
            paymentRepository.storePayment(payment);
            addAndProcess(new PaymentMessage(payment));
        }
    }

    @Override
    public void onPaymentFinished(@NonNull final GenericPayment genericPayment) {
        if (handleEsc(genericPayment)) {
            onRecoverPaymentEscInvalid(paymentRepository.createRecoveryForInvalidESC());
        } else {
            paymentRepository.storePayment(genericPayment);
            addAndProcess(new GenericPaymentMessage(genericPayment));
        }
    }

    @Override
    public void onPaymentFinished(@NonNull final BusinessPayment businessPayment) {
        handleEsc(businessPayment);
        paymentRepository.storePayment(businessPayment);
        addAndProcess(new BusinessPaymentMessage(businessPayment));
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        if (handleEsc(error)) {
            onRecoverPaymentEscInvalid(paymentRepository.createRecoveryForInvalidESC());
        } else {
            addAndProcess(new ErrorMessage(error));
        }
    }

    private boolean handleEsc(@NonNull final MercadoPagoError error) {
        return escManager.manageEscForError(error, paymentRepository.getPaymentData());
    }

    private boolean handleEsc(@NonNull final IPayment payment) {
        return escManager.manageEscForPayment(paymentRepository.getPaymentData(),
            payment.getPaymentStatus(),
            payment.getPaymentStatusDetail());
    }

    private void addAndProcess(@NonNull final Message message) {
        messages.add(message);
        processMessages();
    }

    public void processMessages() {
        //Can't process if handler is null.
        if (handler != null) {
            final PaymentServiceHandler currentHandler = handler.get();
            while (!messages.isEmpty() && currentHandler != null) {
                final Message polledMessage = messages.poll();
                polledMessage.processMessage(currentHandler);
            }
        }
    }

    //region messages

    private interface Message {
        void processMessage(@NonNull final PaymentServiceHandler handler);
    }

    private static class CVVRequiredMessage implements Message {

        @NonNull private final Card card;

        /* default */ CVVRequiredMessage(@NonNull final Card card) {
            this.card = card;
        }

        @Override
        public void processMessage(@NonNull final PaymentServiceHandler handler) {
            handler.onCvvRequired(card);
        }
    }

    private static class RecoverPaymentEscInvalidMessage implements Message {

        private final PaymentRecovery recovery;

        /* default */ RecoverPaymentEscInvalidMessage(final PaymentRecovery recovery) {
            this.recovery = recovery;
        }

        @Override
        public void processMessage(@NonNull final PaymentServiceHandler handler) {
            handler.onRecoverPaymentEscInvalid(recovery);
        }
    }

    private static class PaymentMessage implements Message {

        @NonNull private final Payment payment;

        /* default */ PaymentMessage(@NonNull final Payment payment) {
            this.payment = payment;
        }

        @Override
        public void processMessage(@NonNull final PaymentServiceHandler handler) {
            handler.onPaymentFinished(payment);
        }
    }

    private static class ErrorMessage implements Message {

        @NonNull private final MercadoPagoError error;

        /* default */ ErrorMessage(@NonNull final MercadoPagoError error) {
            this.error = error;
        }

        @Override
        public void processMessage(@NonNull final PaymentServiceHandler handler) {
            handler.onPaymentError(error);
        }
    }

    private static class GenericPaymentMessage implements Message {
        @NonNull private final GenericPayment genericPayment;

        /* default */ GenericPaymentMessage(
            @NonNull final GenericPayment genericPayment) {
            this.genericPayment = genericPayment;
        }

        @Override
        public void processMessage(@NonNull final PaymentServiceHandler handler) {
            handler.onPaymentFinished(genericPayment);
        }
    }

    private static class BusinessPaymentMessage implements Message {
        @NonNull private final BusinessPayment businessPayment;

        /* default */ BusinessPaymentMessage(
            @NonNull final BusinessPayment businessPayment) {
            this.businessPayment = businessPayment;
        }

        @Override
        public void processMessage(@NonNull final PaymentServiceHandler handler) {
            handler.onPaymentFinished(businessPayment);
        }
    }

    private static class VisualPaymentMessage implements Message {
        @Override
        public void processMessage(@NonNull final PaymentServiceHandler handler) {
            handler.onVisualPayment();
        }
    }

    //endregion
}
