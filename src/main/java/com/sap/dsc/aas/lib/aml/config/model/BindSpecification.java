package com.sap.dsc.aas.lib.aml.config.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class BindSpecification {

    protected Map<String, Object> paths = new HashMap<>();

    public Map<String, Object> getProperties() {
        return paths;
    }

    @JsonAnySetter
    public void setProperty(String key, Object value) {
        paths.put(key, value);
    }
}
