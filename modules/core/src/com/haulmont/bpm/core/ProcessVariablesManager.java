/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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
