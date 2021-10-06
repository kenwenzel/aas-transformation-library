package com.sap.dsc.aas.lib.aml.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.dsc.aas.lib.aml.config.pojo.ConfigReference;

public interface GlobalAssetIdReference extends Config {
	ConfigReference getGlobalAssetIdReference();

	@JsonProperty("globalAssetIdReference")
	void setGlobalAssetIdReference(ConfigReference globalAssetIdReference);
}
