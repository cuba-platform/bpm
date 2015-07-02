/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.core.ProcessRuntimeManager;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.cuba.security.entity.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author gorbunkov
 */
@Service(ProcessRuntimeService.NAME)
public class ProcessRuntimeServiceBean implements ProcessRuntimeService {

    @Inject
    protected ProcessRuntimeManager processRuntimeManager;

    @Override
    public ProcInstance startProcess(ProcInstance procInstance, String comment) {
        return processRuntimeManager.startProcess(procInstance, comment);
    }

    @Override
    public ProcInstance startProcess(ProcInstance procInstance, String comment, Map<String, Object> params) {
        return processRuntimeManager.startProcess(procInstance, comment, params);
    }

    @Override
    public ProcInstance cancelProcess(ProcInstance procInstance, String comment) {
        return processRuntimeManager.cancelProcess(procInstance, comment);
    }

    @Override
    public void completeProcTask(ProcTask procTask, String outcome, String comment) {
        processRuntimeManager.completeProcTask(procTask, outcome, comment);
    }

    @Override
    public void completeProcTask(ProcTask procTask, String outcome, String comment, Map<String, Object> processVariables) {
        processRuntimeManager.completeProcTask(procTask, outcome, comment, processVariables);
    }

    @Override
    public long getActiveProcessesCount(ProcDefinition procDefinition) {
        return processRuntimeManager.getActiveProcessesCount(procDefinition);
    }

    @Override
    public void claimProcTask(ProcTask procTask, User user) {
        processRuntimeManager.claimProcTask(procTask, user);
    }

    @Override
    public Object evaluateExpression(String expression, String actExecutionId) {
        return processRuntimeManager.evaluateExpression(expression, actExecutionId);
    }
}