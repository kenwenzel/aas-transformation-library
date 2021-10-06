/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.model;

import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGeneration;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGenerationParameter;
import com.sap.dsc.aas.lib.aml.exceptions.AlreadyDefinedException;

public abstract class IdentifiableConfigSupport implements IdentifiableConfig {
    private ConfigIdGeneration idGeneration;
    private KeyType keyType;
    private KeyElements keyElement;

    public ConfigIdGeneration getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(ConfigIdGeneration idGeneration) {
        this.idGeneration = idGeneration;
    }

    public void setValueId(String valueId) {
        if (this.idGeneration != null) {
            throw new AlreadyDefinedException("idGeneration");
        }
        this.idGeneration = new ConfigIdGeneration();
        ConfigIdGenerationParameter parameter = new ConfigIdGenerationParameter();
        parameter.setValueDefault(valueId);
        this.idGeneration.setParameters(Collections.singletonList(parameter));
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public KeyElements getKeyElement() {
        return keyElement;
    }

    public void setKeyElement(KeyElements keyElement) {
        this.keyElement = keyElement;
    }
}