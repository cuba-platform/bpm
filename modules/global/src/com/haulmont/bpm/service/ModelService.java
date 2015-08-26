/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.rest.RestModel;

/**
 * Service for working with activiti models
 * @author gorbunkov
 * @version $Id$
 */
public interface ModelService {
    String NAME = "bpm_ModelService";

    /**
     * @return an object that contains all necessary information for displaying a model in modeler
     */
    RestModel getModelJson(String actModelId);

    /**
     * Saves activiti model to the database
     */
    void updateModel(String actModelId, String modelName, String modelDescription,
                     String modelJsonStr, String modelSvgStr);

    void updateModel(String actModelId, String modelName, String modelDescription);

    /**
     * Creates new activiti model
     * @param name model name
     * @return activiti model id
     */
    String createModel(String name);

    String updateModelNameInJson(String json, String modelName);
}
