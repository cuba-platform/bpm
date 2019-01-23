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

import com.haulmont.bpm.core.ProcessVariablesManager;
import com.haulmont.bpm.entity.ProcInstance;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;

@Service(ProcessVariablesService.NAME)
public class ProcessVariablesServiceBean implements ProcessVariablesService {

    @Inject
    protected ProcessVariablesManager processVariablesManager;

    @Override
    public Map<String, Object> getVariables(ProcInstance procInstance) {
        return processVariablesManager.getVariables(procInstance);
    }

    @Override
    public void setVariables(String executionId, Map<String, Object> variables) {
        processVariablesManager.setVariables(executionId, variables);
    }

    @Override
    public void setVariables(ProcInstance procInstance, Map<String, Object> variables) {
        processVariablesManager.setVariables(procInstance, variables);
    }

    @Override
    public void setVariable(ProcInstance procInstance, String variableName, Object variableValue) {
        processVariablesManager.setVariable(procInstance, variableName, variableValue);
    }

    @Override
    public Map<String, Object> getVariables(String executionId) {
        return processVariablesManager.getVariables(executionId);
    }

    @Override
    public Object getVariable(ProcInstance procInstance, String variableName) {
        return processVariablesManager.getVariable(procInstance, variableName);
    }

    @Override
    public void setVariable(String executionId, String variableName, Object variableValue) {
        processVariablesManager.setVariable(executionId, variableName, variableValue);
    }

    @Override
    public Object getVariable(String executionId, String variableName) {
        return processVariablesManager.getVariable(executionId, variableName);
    }
}