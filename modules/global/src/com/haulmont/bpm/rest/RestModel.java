/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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
    private String modelJson;

    public RestModel(String modelId, String name, String modelJson) {
        this.modelId = modelId;
        this.name = name;
        this.modelJson = modelJson;
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

    public String getModelJson() {
        return modelJson;
    }

    public void setModelJson(String modelJson) {
        this.modelJson = modelJson;
    }
}