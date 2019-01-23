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

import com.haulmont.bpm.core.ProcessRuntimeManager;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.cuba.security.entity.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;

@Service(ProcessRuntimeService.NAME)
public class ProcessRuntimeServiceBean implements ProcessRuntimeService {

    @Inject
    protected ProcessRuntimeManager processRuntimeManager;

    @Override
    public ProcInstance startProcess(ProcInstance procInstance, String comment, Map<String, Object> params) {
        return processRuntimeManager.startProcess(procInstance, comment, params);
    }

    @Override
    public ProcInstance cancelProcess(ProcInstance procInstance, String comment) {
        return processRuntimeManager.cancelProcess(procInstance, comment);
    }

    @Override
    public void completeProcTask(ProcTask procTask, String outcome, String comment, Map<String, Object> processVariables) {
        processRuntimeManager.completeProcTask(procTask, outcome, comment, processVariables);
    }

    @Override
    public long getActiveProcessesCount(ProcDefinition procDefinition) {
        return processRuntimeManager.getActiveProcInstanceCount(procDefinition);
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