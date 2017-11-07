/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcInstance;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;

import org.springframework.stereotype.Component;
import javax.inject.Inject;
import java.util.Map;

@Component(ProcessVariablesManager.NAME)
public class ProcessVariablesManagerBean implements ProcessVariablesManager{

    @Inject
    protected RuntimeService runtimeService;

    @Override
    public Map<String, Object> getVariables(ProcInstance procInstance) {
        Execution execution = getExecution(procInstance);
        return getVariables(execution.getId());
    }

    @Override
    public void setVariables(ProcInstance procInstance, Map<String, Object> variables) {
        Execution execution = getExecution(procInstance);
        setVariables(execution.getId(), variables);
    }

    @Override
    public Object getVariable(ProcInstance procInstance, String variableName) {
        Execution execution = getExecution(procInstance);
        return getVariable(execution.getId(), variableName);
    }

    @Override
    public void setVariable(ProcInstance procInstance, String variableName, Object variableValue) {
        Execution execution = getExecution(procInstance);
        setVariable(execution.getId(), variableName, variableValue);
    }

    @Override
    public Map<String, Object> getVariables(String executionId) {
        return runtimeService.getVariables(executionId);
    }

    @Override
    public void setVariables(String executionId, Map<String, Object> variables) {
        runtimeService.setVariables(executionId, variables);
    }

    @Override
    public Object getVariable(String executionId, String variableName) {
        return runtimeService.getVariable(executionId, variableName);
    }

    @Override
    public void setVariable(String executionId, String variableName, Object variableValue) {
        runtimeService.setVariable(executionId, variableName, variableValue);
    }

    protected Execution getExecution(ProcInstance procInstance) {
        return runtimeService.createExecutionQuery().processInstanceId(procInstance.getActProcessInstanceId()).singleResult();
    }
}