/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ConfigSupport implements Config {

    private String idShortXPath = "@Name";
    private String configElementId;
    private BindSpecification bindSpecification;
    private Map<String, Object> variables;
    private Map<String, Object> definitions;

    public String getIdShortXPath() {
        return idShortXPath;
    }

    @JsonProperty("idShort_xpath")
    public void setIdShortXPath(String idShortXPath) {
        this.idShortXPath = idShortXPath;
    }

    /**
     * Returns an (optional) Id that can be used to refer to an element of the config
     *
     * @return The config element's Id
     */
    public String getConfigElementId() {
        return configElementId;
    }

    public void setConfigElementId(String configElementId) {
        this.configElementId = configElementId;
    }

    @Override
    public BindSpecification getBindSpecification() {
        return bindSpecification;
    }

    @Override
    public void setBindSpecification(BindSpecification bindSpecification) {
        this.bindSpecification = bindSpecification;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    @Override
    public Map<String, Object> getDefinitions() {
        return definitions;
    }

    @Override
    public void setDefinitions(Map<String, Object> definitions) {
        this.definitions = definitions;
    }
}
