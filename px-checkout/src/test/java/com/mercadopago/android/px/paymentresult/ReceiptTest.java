package com.mercadopago.android.px.paymentresult;

import com.mercadopago.android.px.components.Receipt;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.mocks.PaymentResults;
import junit.framework.Assert;
import org.junit.Test;

public class ReceiptTest {

    @Test
    public void testReceiptPropsAreValid() {
        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
        String stringPaymentId = String.valueOf(paymentResult.getPaymentId());
        final Receipt.ReceiptProps receiptProps = new Receipt.ReceiptProps(stringPaymentId);
        final Receipt receipt = new Receipt(receiptProps);
        Assert.assertNotNull(receipt.props.receiptId);
        Assert.assertEquals(receipt.props.receiptId, stringPaymentId);
    }
}
