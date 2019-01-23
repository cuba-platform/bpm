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

public interface ProcessRuntimeManager {

    String NAME = "bpm_ProcessRuntimeManager";

    /**
     * Starts activiti process and updates the passed {@code procInstance}. When the process is started the following
     * process variables are automatically created:
     * <ul>
     * <li>bpmProcInstanceId - current ProcInstance object id</li>
     * <li>entityId - linked entity id</li>
     * <li>entityName - linked entity name</li>
     * </ul>
     * <p>
     * If the process instance passed is new then it will persisted together with its process actors
     *
     * @param procInstance process instance.
     * @param comment      start process comment. Can be null.
     * @param variables    variables that should be added to the process. Can be null.
     * @return updated ProcInstance
     */
    ProcInstance startProcess(ProcInstance procInstance, String comment, Map<String, Object> variables);

    /**
     * Method deletes process instance from activiti engine, updates the {@code procInstance} and all tasks of this
     * procInstance
     *
     * @param procInstance procInstance to be cancelled
     * @param comment      cancellation comment that will be written to the {@code procInstance}
     * @return modified procInstance
     */
    ProcInstance cancelProcess(ProcInstance procInstance, String comment);

    /**
     * Finds all users who are process actors for given process role and returns these users ids.
     *
     * @return list of users' ids string representations
     */
    List<String> getTaskAssigneeList(UUID procInstanceId, String procRoleCode);

    /**
     * Finds a single user who is a process actor for given process role. If multiple users are assigned to the role
     * then an exception will be thrown.
     *
     * @return string representation of user id
     */
    String getSingleTaskAssignee(UUID procInstanceId, String procRoleCode);

    /**
     * Signals for activiti engine that a UserTask is completed and updates a procTask object
     *
     * @param procTask         process task to be completed
     * @param outcome          an outcome that will be written to the {@code procTask}
     * @param comment          a comment that will be written to the {@code procTask}
     * @param processVariables variables to be set into activiti process
     */
    void completeProcTask(ProcTask procTask, String outcome, String comment, Map<String, Object> processVariables);

    /**
     * Sets outcome and endDate properties to the given {@link ProcTask}. In contrast to {@link
     * #completeProcTask(ProcTask, String, String, Map)} this method doesn't set any process variables that store an
     * outcome
     */
    void completeProcTaskOnTimer(UUID procTaskId, String outcome);

    /**
     * Claims responsibility for the task
     *
     * @param procTask process task to claim
     * @param user     user that claims the task
     */
    void claimProcTask(ProcTask procTask, User user);

    /**
     * @return a number of active ProcInstance objects with a given procDefinition
     */
    long getActiveProcInstanceCount(ProcDefinition procDefinition);

    /**
     * Evaluates an UEL expression with a context of activiti process
     *
     * @param expression     expression string
     * @param actExecutionId activiti execution id
     * @return expression evaluation result
     */
    Object evaluateExpression(String expression, String actExecutionId);

    /**
     * Creates new {@link ProcTask} based on activiti {@link TaskEntity}
     *
     * @param actTask activiti TaskEntity
     * @return created ProcTask
     */
    ProcTask createProcTask(TaskEntity actTask);

    /**
     * Assign an owner to the task that doesn't have an owner yet and updates a joined ProcTask
     *
     * @param taskEntity activiti TaskEntity
     */
    void assignProcTask(TaskEntity taskEntity);

    /**
     * Creates new {@link ProcTask} that doesn't have assigned user
     *
     * @param taskEntity activiti TaskEntity
     * @return creates ProcTask
     */
    ProcTask createNotAssignedProcTask(TaskEntity taskEntity);
}