package com.sap.dsc.aas.lib.aml.config.model;

import io.adminshell.aas.v3.model.HasSemantics;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface ConfigFromAttribute extends Config, HasSemantics {

    String getXPath();

    @JsonProperty("from_xpath")
    void setXPath(String xPath);

    @JsonProperty("from_attributeName")
    void setAttributeName(String attributeName);

    @JsonProperty("semanticId_str")
    void setSemanticIdFromString(String id);

    BindSpecification getBindSpecification();

    @JsonProperty("@bind")
    void setBindSpecification(BindSpecification bindSpecification);

    String getIdShortXPath();

    @JsonProperty("idShort_xpath")
    void setIdShortXPath(String idShortXPath);

    String getLangXPath();

    @JsonProperty("langXPath")
    void setLangXPath(String langXPath);

    String getValueXPath();

    @JsonProperty("valueXPath")
    void setValueXPath(String valueXPath);

    String getMinValueXPath();

    void setMinValueXPath(String minValueXPath);

    String getMaxValueXPath();

    void setMaxValueXPath(String maxValueXPath);

    String getMimeTypeXPath();

    void setMimeTypeXPath(String mimeTypeXPath);
}
