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

import com.haulmont.bpm.entity.ProcInstance;

import java.util.Map;

/**
 * Class for working with activiti process variables
 *
 */
public interface ProcessVariablesManager {
    String NAME = "bpm_ProcessVariablesManager";

    Map<String, Object> getVariables(ProcInstance procInstance);

    void setVariables(ProcInstance procInstance, Map<String, Object> variables);

    Object getVariable(ProcInstance procInstance, String variableName);

    void setVariable(ProcInstance procInstance, String variableName, Object variableValue);

    Map<String, Object> getVariables(String executionId);

    void setVariables(String executionId, Map<String, Object> variables);

    Object getVariable(String executionId, String variableName);

    void setVariable(String executionId, String variableName, Object variableValue);
}
