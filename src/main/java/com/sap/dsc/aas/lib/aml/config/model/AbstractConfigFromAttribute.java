/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.model;

import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import net.enilink.composition.traits.Behaviour;
import java.util.ArrayList;
import java.util.List;

import com.sap.dsc.aas.lib.aml.exceptions.AlreadyDefinedException;

public abstract class AbstractConfigFromAttribute extends AbstractConfig
    implements ConfigFromAttribute, Behaviour<ConfigFromAttribute> {

    private String xPath, langXPath, valueXPath, minValueXPath, maxValueXPath, mimeTypeXPath;
    private BindSpecification bindSpecification;

    @Override
    public String getXPath() {
        return xPath;
    }

    @Override
    public void setXPath(String xPath) {
        if (this.xPath != null) {
            throw new AlreadyDefinedException("from_attributeName");
        }
        this.xPath = xPath;
    }

    @Override
    public void setAttributeName(String attributeName) {
        if (this.xPath != null) {
            throw new AlreadyDefinedException("from_xpath", this.xPath);
        }
        this.xPath = "caex:Attribute[@Name='" + attributeName + "']";
    }

    /**
     * Syntax sugar to allow defining semanticId (make config JSON more compact)
     *
     * @param id The hardcoded id value
     */
    @Override
    public void setSemanticIdFromString(String id) {
        if (getSemanticId() != null) {
            throw new AlreadyDefinedException("semanticId");
        }
        List<Key> keys = new ArrayList<>();
        Key key = new DefaultKey();
        key.setIdType(KeyType.IRDI);
        key.setType(KeyElements.CONCEPT_DESCRIPTION);
        key.setValue(id);
        keys.add(key);
        Reference semanticId = new DefaultReference();
        semanticId.setKeys(keys);
        setSemanticId(semanticId);
    }

    @Override
    public void setSemanticId(Reference semanticId) {
        /*if (getSemanticId() != null) {
            throw new AlreadyDefinedException("semanticId_str");
        }*/
    }

    @Override
    public BindSpecification getBindSpecification() {
        return bindSpecification;
    }

    @Override
    public void setBindSpecification(BindSpecification bindSpecification) {
        this.bindSpecification = bindSpecification;
    }

    public String getLangXPath() {
        return langXPath;
    }

    public void setLangXPath(String langXPath) {
        this.langXPath = langXPath;
    }

    public String getValueXPath() {
        return valueXPath;
    }

    public void setValueXPath(String valueXPath) {
        this.valueXPath = valueXPath;
    }

    public String getMinValueXPath() {
        return minValueXPath;
    }

    public void setMinValueXPath(String minValueXPath) {
        this.minValueXPath = minValueXPath;
    }

    public String getMaxValueXPath() {
        return maxValueXPath;
    }

    public void setMaxValueXPath(String maxValueXPath) {
        this.maxValueXPath = maxValueXPath;
    }

    public String getMimeTypeXPath() {
        return mimeTypeXPath;
    }

    public void setMimeTypeXPath(String mimeTypeXPath) {
        this.mimeTypeXPath = mimeTypeXPath;
    }
}
