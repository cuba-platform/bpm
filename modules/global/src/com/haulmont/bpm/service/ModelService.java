/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.rest.RestModel;

/**
 * Service for working with activiti models
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
