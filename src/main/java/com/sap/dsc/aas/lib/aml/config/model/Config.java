package com.sap.dsc.aas.lib.aml.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface Config {
	String getIdShortXPath();

	@JsonProperty("idShort_xpath")
	void setIdShortXPath(String idShortXPath);

	/**
	 * Returns an (optional) Id that can be used to refer to an element of the config
	 *
	 * @return The config element's Id
	 */
	String getConfigElementId();

	void setConfigElementId(String configElementId);
}
