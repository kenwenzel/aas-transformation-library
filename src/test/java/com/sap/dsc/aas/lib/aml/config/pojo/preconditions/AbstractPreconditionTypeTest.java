/* 
  SPDX-FileCopyrightText: (C)2021 SAP SE or an affiliate company and aas-transformation-library contributors. All rights reserved. 

  SPDX-License-Identifier: Apache-2.0 
 */
package com.sap.dsc.aas.lib.aml.config.pojo.preconditions;

import com.fasterxml.jackson.databind.ObjectMapper;

abstract class AbstractPreconditionTypeTest {

    protected ObjectMapper objectMapper;

    void setup() {
        this.objectMapper = new ObjectMapper();
    }

}
