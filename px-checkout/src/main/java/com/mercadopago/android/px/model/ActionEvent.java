package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;

public class ActionEvent extends Event {

    private String screenId;
    private String screenName;
    private String action;
    private String category;
    private String label;
    private String value;

    private ActionEvent(final Builder builder) {
        super();
        setType(TYPE_ACTION);
        setFlowId(builder.flowId);
        setTimestamp(System.currentTimeMillis());
        setProperties(builder.properties);
        screenId = builder.screenId;
        screenName = builder.screenName;
        action = builder.action;
        category = builder.category;
        label = builder.label;
        value = builder.value;
    }

    public String getScreenId() {
        return screenId;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getAction() {
        return action;
    }

    public String getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public static class Builder {

        private String flowId;
        private String screenId;
        private String screenName;
        private String action;
        private String category;
        private String label;
        private String value;
        private Map<String, String> properties = new HashMap<>();

        public Builder setFlowId(@NonNull final String flowId) {
            this.flowId = flowId;
            return this;
        }

        public Builder setScreenId(@NonNull final String screenId) {
            this.screenId = screenId;
            return this;
        }

        public Builder setScreenName(@NonNull final String screenName) {
            this.screenName = screenName;
            return this;
        }

        public Builder setAction(@NonNull final String action) {
            this.action = action;
            return this;
        }

        public Builder setCategory(@NonNull final String category) {
            this.category = category;
            return this;
        }

        public Builder setLabel(@NonNull final String label) {
            this.label = label;
            return this;
        }

        public Builder setValue(@NonNull final String value) {
            this.value = value;
            return this;
        }

        public Builder addProperty(@NonNull String key, @NonNull String value) {
            properties.put(key, value);
            return this;
        }

        public ActionEvent build() {
            return new ActionEvent(this);
        }
    }
}
