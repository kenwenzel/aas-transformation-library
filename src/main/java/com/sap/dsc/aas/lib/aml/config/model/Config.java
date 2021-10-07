package com.sap.dsc.aas.lib.aml.config.model;

import java.util.Map;

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

	BindSpecification getBindSpecification();

	@JsonProperty("@bind")
	void setBindSpecification(BindSpecification bindSpecification);

	Map<String, Object> getVariables();

	@JsonProperty("@vars")
	void setVariables(Map<String, Object> variables);

	Map<String, Object> getDefinitions();

	@JsonProperty("@definitions")
	void setDefinitions(Map<String, Object> definitions);
}
