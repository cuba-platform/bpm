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

import org.activiti.bpmn.model.ExtensionElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Helper class for working with extension elements of BPMN model elements
 *
 */
public interface ExtensionElementsManager {

    String NAME = "bpm_ExtensionElementsManager";

    Map<String, List<ExtensionElement>> getFlowElementExtensionElements(String actProcessDefinitionId, String actFlowElementDefinitionKey);

    Map<String, List<ExtensionElement>> getStartExtensionElements(String actProcessDefinitionId);

    Map<String, List<ExtensionElement>> getProcessExtensionElements(String actProcessDefinitionId);

    @Nullable
    String getTimerOutcome(String actProcessDefinitionId, String actBoundaryEventDefinitionKey);

    @Nullable
    String getTaskProcRole(String actProcessDefinitionId, String actTaskDefinitionKey);
}
