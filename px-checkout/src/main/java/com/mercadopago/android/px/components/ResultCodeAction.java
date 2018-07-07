package com.mercadopago.android.px.components;

public class ResultCodeAction extends Action {

    public final int resultCode;

    public ResultCodeAction(int resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        return "Responder con un código de resultado.";
    }
}
