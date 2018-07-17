package com.mercadopago.android.px.plugins;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.CheckoutStore;
import java.util.Map;

public abstract class DataInitializationTask {

    public static final String KEY_INIT_SUCCESS = "init_success";

    private final Map<String, Object> data;
    private Thread taskThread;

    public DataInitializationTask(@NonNull final Map<String, Object> defaultData) {
        data = CheckoutStore.getInstance().getData();
        data.clear();
        data.putAll(defaultData);
    }

    /* async init */
    public void execute(final DataInitializationCallbacks callbacks) {
        taskThread = new Thread(new Runnable() {
            @Override
            public void run() {
                initPlugins(callbacks);
            }
        });
        taskThread.start();
    }

    /* sync init */
    public void initPlugins(final DataInitializationCallbacks callback) {
        try {
            onLoadData(data);
            if (!taskThread.isInterrupted()) {
                callback.onDataInitialized(data);
            }
        } catch (final Exception e) {
            callback.onFailure(e, data);
        }
    }

    public void cancel() {
        if (taskThread != null && taskThread.isAlive() && !taskThread.isInterrupted()) {
            taskThread.interrupt();
        }
    }

    public abstract void onLoadData(@NonNull final Map<String, Object> data);

    public interface DataInitializationCallbacks {
        void onDataInitialized(@NonNull final Map<String, Object> data);

        void onFailure(@NonNull final Exception e, @NonNull final Map<String, Object> data);
    }
}