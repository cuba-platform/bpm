/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcInstance;

import java.util.Map;

/**
 * Class provides a way to work process variables from a client tier. For documentation see {@code ProcessVariablesManager}
 *
 */
public interface ProcessVariablesService {
    String NAME = "bpm_ProcessVariablesService";

    Map<String, Object> getVariables(ProcInstance procInstance);

    void setVariables(String executionId, Map<String, Object> variables);

    void setVariables(ProcInstance procInstance, Map<String, Object> variables);

    void setVariable(ProcInstance procInstance, String variableName, Object variableValue);

    Map<String, Object> getVariables(String executionId);

    Object getVariable(ProcInstance procInstance, String variableName);

    void setVariable(String executionId, String variableName, Object variableValue);

    Object getVariable(String executionId, String variableName);
}
