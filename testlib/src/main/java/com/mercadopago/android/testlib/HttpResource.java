package com.mercadopago.android.testlib;

import android.support.test.espresso.IdlingRegistry;
import com.mercadopago.android.testlib.espresso.OkHttp3IdlingResource;
import okhttp3.OkHttpClient;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class HttpResource implements TestRule {

    private static final String RES_NAME_OK_HTTP = "OK_HTTP";

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final OkHttpClient client = getClient();
                final OkHttp3IdlingResource okHttp3IdlingResource = OkHttp3IdlingResource.create(RES_NAME_OK_HTTP, client);
                IdlingRegistry.getInstance().register(okHttp3IdlingResource);
                base.evaluate();
                IdlingRegistry.getInstance().unregister(okHttp3IdlingResource);
            }
        };
    }

    protected abstract OkHttpClient getClient();
}
