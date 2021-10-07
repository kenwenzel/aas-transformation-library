/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.model;

import io.adminshell.aas.v3.model.AssetInformation;

import com.sap.dsc.aas.lib.aml.config.pojo.AbstractConfig;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;
import com.sap.dsc.aas.lib.aml.exceptions.AlreadyDefinedException;

public abstract class AssetInformationConfig extends AbstractConfig implements AssetInformation, KindTypeXPath, GlobalAssetIdReference {
    private String kindTypeXPath = "TYPE";
    private ConfigIdGeneration idGeneration;

    private ConfigReference globalAssetIdReference;

    public ConfigIdGeneration getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(ConfigIdGeneration idGeneration) {
        this.idGeneration = idGeneration;
    }

    @Override
    public String getKindTypeXPath() {
        return kindTypeXPath;
    }

    @Override
    public void setKindTypeXPath(String kindTypeXPath) {
        this.kindTypeXPath = kindTypeXPath;
    }

    @Override
    public ConfigReference getGlobalAssetIdReference() {
        return globalAssetIdReference;
    }

    @Override
    public void setGlobalAssetIdReference(ConfigReference globalAssetIdReference) {
        if (this.globalAssetIdReference != null) {
            throw new AlreadyDefinedException("globalAssetIdReference");
        }
        this.globalAssetIdReference = globalAssetIdReference;
    }

}
