package com.mercadopago.tracking.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import com.mercadopago.tracking.core.Settings;
import com.mercadopago.tracking.core.TLSSocketFactory;
import java.io.File;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public final class HttpClientUtil {

    private static final int TIMEOUT = 20;
    private static final int TIMEOUT_WRITE = 20;
    private static final int TIMEOUT_READ = 20;
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String CACHE_DIR_NAME = "PX_OKHTTP_CACHE_TRACKING";

    private static OkHttpClient client;

    private HttpClientUtil() {
    }

    public static synchronized OkHttpClient getClient(final Context context) {
        if (client == null) {
            client = createClient(context);
        }
        return client;
    }

    @NonNull
    private static OkHttpClient createClient(final Context context) {
        // Set log info
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(Settings.OKHTTP_LOGGING);

        // Set cache size
        final Cache cache =
            new Cache(new File(String.format("%s%s", context.getCacheDir().getPath(), CACHE_DIR_NAME)), CACHE_SIZE);

        // Set client
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor(interceptor)
            .build();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            try {
                client = TLSSocketFactory.enforceTls(client);
            } catch (final Exception e) {
                // Do nothing
            }
        }

        return client;
    }
}
