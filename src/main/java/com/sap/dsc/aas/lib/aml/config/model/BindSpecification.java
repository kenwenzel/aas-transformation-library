package com.sap.dsc.aas.lib.aml.config.model;

import java.util.HashMap;
import java.util.Map;

public class BindSpecification {

    protected Map<String, Object> bindings = new HashMap<>();

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBinding(String key, Object value) {
        bindings.put(key, value);
    }
}
