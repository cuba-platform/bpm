/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.cuba.security.entity.User;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author gorbunkov
 */
public interface ProcessRuntimeService {
    String NAME = "bpm_ProcessRuntimeService";

    ProcInstance startProcess(ProcInstance procInstance, String comment, Map<String, Object> params);

    ProcInstance cancelProcess(ProcInstance procInstance, String comment);

    void completeProcTask(ProcTask procTask, String outcome, String comment, @Nullable Map<String, Object> processVariables);

    long getActiveProcessesCount(ProcDefinition procDefinition);

    void claimProcTask(ProcTask procTask, User user);

    Object evaluateExpression(String expression, String actExecutionId);
}