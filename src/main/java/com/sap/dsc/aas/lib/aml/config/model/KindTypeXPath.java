package com.sap.dsc.aas.lib.aml.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;

public interface KindTypeXPath extends Config {
	String getKindTypeXPath();

	@JsonProperty("kindType_xpath")
	void setKindTypeXPath(String kindTypeXPath);
}
