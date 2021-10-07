/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.dsc.aas.lib.aml.config.model.ConfigAmlToAas;
import com.sap.dsc.aas.lib.aml.config.model.Mapping;

public class ConfigLoader3Test {

    public static final String PATH_SIMPLE_CONFIG = "src/test/resources/config/simpleConfig3.json";
    private ConfigLoader2 classUnderTest;

    @BeforeEach
    void setup() {
        classUnderTest = new ConfigLoader2();
    }

    @Test
    void loadFromFile() throws IOException {
        ConfigAmlToAas result = classUnderTest.loadConfig(PATH_SIMPLE_CONFIG);

        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo("1.0.0");
        assertThat(result.getAasVersion()).isEqualTo("3.0RC01");
        assertThat(result.getMappings()).isNotEmpty();

        Mapping mapping = result.getMappings().get(0);
        //assertThat(mapping.getConfigAssetInformation().getKindTypeXPath()).contains("TYPE");
        assertThat(mapping.getSubmodels()).hasSize(6);
    }
}
