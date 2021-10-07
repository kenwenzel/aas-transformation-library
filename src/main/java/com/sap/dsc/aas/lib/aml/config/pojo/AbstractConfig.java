/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.model.Config;

public abstract class AbstractConfig {

    private String idShortXPath = "@Name";
    private String configElementId;

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

}
