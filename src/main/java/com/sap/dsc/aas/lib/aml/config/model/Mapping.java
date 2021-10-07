/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.model;

import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.Submodel;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGeneration;

public class Mapping extends ConfigSupport {

    private String XPath;
    private AssetAdministrationShell configAssetShell;
    private AssetInformation configAssetInformation;
    private List<Submodel> submodels;
    private ConfigIdGeneration idGeneration;

    public String getXPath() {
        return XPath;
    }

    @JsonProperty("from_xpath")
    public void setXPath(String xPath) {
        this.XPath = xPath;
    }

    public AssetInformation getConfigAssetInformation() {
        return configAssetInformation;
    }

    @JsonProperty("assetInformation")
    public void setConfigAssetInformation(AssetInformation configAssetInformation) {
        this.configAssetInformation = configAssetInformation;
    }

    public List<Submodel> getSubmodels() {
        return submodels;
    }

    @JsonProperty("submodels")
    public void setSubmodels(List<Submodel> submodels) {
        this.submodels = submodels;
    }

    public ConfigIdGeneration getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(ConfigIdGeneration idGeneration) {
        this.idGeneration = idGeneration;
    }

    public AssetAdministrationShell getConfigAssetShell() {
        return configAssetShell;
    }

    @JsonProperty("assetShell")
    public void setConfigAssetShell(AssetAdministrationShell configAssetShell) {
        this.configAssetShell = configAssetShell;
    }
}
