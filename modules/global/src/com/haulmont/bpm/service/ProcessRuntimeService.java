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

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.cuba.security.entity.User;

import javax.annotation.Nullable;
import java.util.Map;

public interface ProcessRuntimeService {
    String NAME = "bpm_ProcessRuntimeService";

    ProcInstance startProcess(ProcInstance procInstance, String comment, Map<String, Object> params);

    ProcInstance cancelProcess(ProcInstance procInstance, String comment);

    void completeProcTask(ProcTask procTask, String outcome, String comment, @Nullable Map<String, Object> processVariables);

    long getActiveProcessesCount(ProcDefinition procDefinition);

    void claimProcTask(ProcTask procTask, User user);

    Object evaluateExpression(String expression, String actExecutionId);
}