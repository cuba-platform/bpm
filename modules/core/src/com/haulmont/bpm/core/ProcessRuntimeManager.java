/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcActor;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.cuba.security.entity.User;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author gorbunkov
 * @version $Id$
 */
public interface ProcessRuntimeManager {

    String NAME = "bpm_ProcessRuntimeManager";

    ProcInstance startProcess(ProcInstance procInstance, String comment, Map<String, Object> variables);

//    ProcInstance startProcess(ProcDefinition procDefinition, String comment, Map<String, Object> variables);

    ProcInstance cancelProcess(ProcInstance procInstance, String comment);

    @Nullable
    ProcActor findProcActor(UUID procInstanceId, String procRoleCode, UUID userId);

    String getSingleTaskAssignee(UUID procInstanceId, String procRoleCode);

    List<String> getTaskAssigneeList(UUID procInstanceId, String procRoleCode);

    void completeProcTask(ProcTask procTask, String outcome, String comment);

    void completeProcTask(ProcTask procTask, String outcome, String comment, Map<String, Object> processVariables);

    void claimProcTask(ProcTask procTask, User user);

    long getActiveProcessesCount(ProcDefinition procDefinition);

    Object evaluateExpression(String expression, String actExecutionId);

    @Authenticated
    ProcTask createProcTask(TaskEntity actTask);

    @Authenticated
    void assignProcTask(TaskEntity taskEntity);

    @Authenticated
    ProcTask createNotAssignedProcTask(TaskEntity taskEntity);
}