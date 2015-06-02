/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.rest;

import java.io.Serializable;

/**
 * Class is used for transporting model information necessary for modeler from middleware to web tier
 * @author gorbunkov
 * @version $Id$
 */
public class RestModel implements Serializable {

    private String modelId;
    private String name;
    private String model;

    public RestModel(String modelId, String name, String model) {
        this.modelId = modelId;
        this.name = name;
        this.model = model;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}