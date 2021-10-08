package com.sap.dsc.aas.lib.aml.config.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interface for the configuration of AAS model templates.
 */
public interface Config {
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
