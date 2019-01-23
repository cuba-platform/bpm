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

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;

import javax.annotation.Nullable;

public interface ProcessRepositoryManager {

    String NAME = "bpm_ProcessRepositoryManager";

    /**
     * Creates or updates a ProcDefinition and deploys process from a given path to Activiti engine.
     * @param path path to an XML file with BPMN process definition
     * @param procDefinition ProcDefinition that will be linked with deployed BPMN process. If null
     *                       then new ProcDefinition instance will be created
     * @param procModel ProcModel that will be linked with a deployed process. May be null.
     * @return created or modified ProcInstance object
     */
    ProcDefinition deployProcessFromPath(String path, @Nullable ProcDefinition procDefinition, @Nullable ProcModel procModel);

    /**
     * Creates or updates a ProcDefinition and deploys a given BPMN XML to Activiti engine.
     * @param xml content of BPMN XML
     * @param procDefinition ProcDefinition that will be linked with deployed BPMN process. If null
     *                       then new ProcDefinition instance will be created
     * @param procModel ProcModel that will be linked with a deployed process. May be null.
     * @return created or modified ProcInstance object
     */
    ProcDefinition deployProcessFromXml(String xml, @Nullable ProcDefinition procDefinition, @Nullable ProcModel procModel);

    /**
     * Returns an XML representation of BPMN process
     * @param actProcessDefinitionId activiti process definition id
     * @return a string with XML representation of BPMN process
     */
    String getProcessDefinitionXml(String actProcessDefinitionId);

    /**
     * Finds a process model by its activiti id and converts model JSON to BPMN process XML
     * @param actModelId activiti model id
     * @return string with XML BPMN process
     */
    String convertModelToProcessXml(String actModelId);

    /**
     * Deletes a process from Activiti engine
     * @param actProcessDefinitionId activiti process definition id
     */
    void undeployProcess(String actProcessDefinitionId);
}