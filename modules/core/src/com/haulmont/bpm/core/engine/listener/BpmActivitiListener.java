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

package com.haulmont.bpm.core.engine.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.haulmont.bpm.core.BpmAppContextListener;
import com.haulmont.bpm.core.ExtensionElementsManager;
import com.haulmont.bpm.core.ProcessRepositoryManager;
import com.haulmont.bpm.core.ProcessRuntimeManager;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.app.Authentication;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BpmActivitiListener implements ActivitiEventListener {

    private static final Logger log = LoggerFactory.getLogger(BpmAppContextListener.class);

    protected ProcessRuntimeManager processRuntimeManager;
    protected Persistence persistence;
    protected final TimeSource timeSource;
    protected final ProcessRepositoryManager processRepositoryManager;
    protected final Metadata metadata;
    protected final Authentication authentication;
    protected final UserSessionSource userSessionSource;
    protected final ExtensionElementsManager extensionElementsManager;

    public BpmActivitiListener() {
        processRepositoryManager = AppBeans.get(ProcessRepositoryManager.class);
        processRuntimeManager = AppBeans.get(ProcessRuntimeManager.class);
        persistence = AppBeans.get(Persistence.class);
        timeSource = AppBeans.get(TimeSource.class);
        metadata = AppBeans.get(Metadata.class);
        authentication = AppBeans.get(Authentication.class);
        userSessionSource = AppBeans.get(UserSessionSource.class);
        extensionElementsManager = AppBeans.get(ExtensionElementsManager.class);
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
            case TASK_CREATED:
                TaskEntity task = (TaskEntity) ((ActivitiEntityEvent) event).getEntity();
                UUID bpmProcTaskId = (UUID) task.getVariableLocal("bpmProcTaskId");
                // bpmProcTaskId is not set in case of group task that doesn't have a particular assignee
                // but only potential ones. In other cases TASK_ASSIGNED event will be fired first and the variable
                // will be already set
                if (bpmProcTaskId == null) {
                    ProcTask procTask = processRuntimeManager.createNotAssignedProcTask(task);
                    task.setVariableLocal("bpmProcTaskId", procTask.getId());
                }
                break;
            case TASK_ASSIGNED:
                task = (TaskEntity) ((ActivitiEntityEvent) event).getEntity();
                bpmProcTaskId = (UUID) task.getVariableLocal("bpmProcTaskId");
                // bpmProcTaskId will be not null if the task is claimed
                if (bpmProcTaskId == null) {
                    ProcTask procTask = processRuntimeManager.createProcTask(task);
                    task.setVariableLocal("bpmProcTaskId", procTask.getId());
                } else {
                    processRuntimeManager.assignProcTask(task);
                }
                break;
            case PROCESS_COMPLETED:
                onProcessCompleted(event);
                break;
            case TIMER_FIRED:
                TaskService taskService = event.getEngineServices().getTaskService();
                TaskEntity actTask = (TaskEntity) taskService.createTaskQuery().executionId(event.getExecutionId()).singleResult();
                if (actTask == null) break;
                bpmProcTaskId = (UUID) actTask.getVariableLocal("bpmProcTaskId");
                if (bpmProcTaskId != null) {
                    TimerEntity timerEntity = (TimerEntity) ((ActivitiEntityEvent) event).getEntity();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        JsonNode jsonNode = objectMapper.readTree(timerEntity.getJobHandlerConfiguration());
                        String actBoundaryEventDefinitionKey = jsonNode.get("activityId").textValue();
                        String timerOutcome = extensionElementsManager.getTimerOutcome(event.getProcessDefinitionId(), actBoundaryEventDefinitionKey);
                        actTask.setVariableLocal("timerOutcome", timerOutcome);
                    } catch (Exception e) {
                        log.error("Error on evaluating the timer outcome", e);
                    }
                }
                break;
            case ACTIVITY_CANCELLED:
                taskService = event.getEngineServices().getTaskService();
                actTask = (TaskEntity) taskService.createTaskQuery().executionId(event.getExecutionId()).singleResult();
                if (actTask == null) break;
                bpmProcTaskId = (UUID) actTask.getVariableLocal("bpmProcTaskId");
                String timerOutcome = (String) actTask.getVariableLocal("timerOutcome");
                if (bpmProcTaskId != null && !Strings.isNullOrEmpty(timerOutcome)) {
                    processRuntimeManager.completeProcTaskOnTimer(bpmProcTaskId, timerOutcome);
                }
                break;
            case ENTITY_DELETED:
                //if multi-instance task has some completion condition, then Activiti task entities that are not
                // completed by user will be removed. We must set endDate to corresponding uncompleted BpmProcTasks
                if (((ActivitiEntityEvent) event).getEntity() instanceof TaskEntity) {
                    task = (TaskEntity) ((ActivitiEntityEvent) event).getEntity();
                    bpmProcTaskId = (UUID) task.getVariableLocal("bpmProcTaskId");
                    if (bpmProcTaskId != null) {
                        ProcTask procTask = persistence.getEntityManager().find(ProcTask.class, bpmProcTaskId);
                        if (procTask != null && procTask.getEndDate() == null) {
                            procTask.setEndDate(timeSource.currentTimestamp());
                            procTask.setOutcome("completedAutomatically");
                        }
                    }
                }
        }
    }

    protected void onProcessCompleted(ActivitiEvent event) {
        String processInstanceId = event.getProcessInstanceId();
        UUID bpmProcInstanceId = (UUID) ((ExecutionEntity) ((ActivitiEntityEvent) event).getEntity()).getVariable("bpmProcInstanceId");
        if (bpmProcInstanceId == null)
            throw new BpmException("No 'bpmProcInstanceId' process variable defined for activiti process " + processInstanceId);

        Persistence persistence = AppBeans.get(Persistence.class);
        EntityManager em = persistence.getEntityManager();
        ProcInstance procInstance = em.find(ProcInstance.class, bpmProcInstanceId);
        if (procInstance == null) {
            throw new BpmException("ProcInstance with id " + bpmProcInstanceId + " not found");
        }

        procInstance.setEndDate(timeSource.currentTimestamp());
        procInstance.setActive(false);
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }
}