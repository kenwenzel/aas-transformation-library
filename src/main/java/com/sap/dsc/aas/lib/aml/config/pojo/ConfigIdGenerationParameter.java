/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.exceptions.AlreadyDefinedException;

public class ConfigIdGenerationParameter {

    private String xPath;
    private String valueDefault;
    private String idGenerationName;
    private ConfigIdGenerationFinalizeFunction finalizeFunction = ConfigIdGenerationFinalizeFunction.CONCATENATE;

    public String getXPath() {
        return xPath;
    }

    @JsonProperty("from_xpath")
    public void setXPath(String xPath) {
        if (idGenerationName != null) {
            throw new AlreadyDefinedException("from_idGenerationName", idGenerationName);
        }
        this.xPath = xPath;
    }

    public String getValueDefault() {
        return valueDefault;
    }

    @JsonProperty("from_string")
    public void setValueDefault(String valueDefault) {
        this.valueDefault = valueDefault;
    }

    public String getIdGenerationName() {
        return idGenerationName;
    }

    @JsonProperty("from_idGenerationName")
    public void setIdGenerationName(String idName) {
        if (xPath != null) {
            throw new AlreadyDefinedException("from_xpath", xPath);
        }
        this.idGenerationName = idName;
    }

    public ConfigIdGenerationFinalizeFunction getFinalizeFunction() {
        return finalizeFunction;
    }

    public void setFinalizeFunction(String finalizeFunction) {
        this.finalizeFunction = ConfigIdGenerationFinalizeFunction.valueOf(finalizeFunction.toUpperCase());
    }

}
