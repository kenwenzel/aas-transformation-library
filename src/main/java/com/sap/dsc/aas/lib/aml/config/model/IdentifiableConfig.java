package com.sap.dsc.aas.lib.aml.config.model;

import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;

import com.sap.dsc.aas.lib.aml.config.pojo.ConfigIdGeneration;

public interface IdentifiableConfig extends Config {

    ConfigIdGeneration getIdGeneration();

    void setIdGeneration(ConfigIdGeneration idGeneration);

    void setValueId(String valueId);

    KeyType getKeyType();

    void setKeyType(KeyType keyType);

    KeyElements getKeyElement();

    void setKeyElement(KeyElements keyElement);
}
