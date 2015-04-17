/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core.engine.listener;

import com.google.common.base.Strings;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.bpm.core.ProcessRepositoryManager;
import com.haulmont.bpm.core.ProcessRuntimeManager;
import com.haulmont.bpm.entity.ProcRole;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcActor;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.app.Authentication;
import com.haulmont.cuba.security.app.UserSessionService;
import com.haulmont.cuba.security.entity.User;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class BpmActivitiListener implements ActivitiEventListener {

    protected ProcessRuntimeManager processRuntimeManager;
    protected Persistence persistence;
    protected final TimeSource timeSource;
    protected final ProcessRepositoryManager processRepositoryManager;
    protected final Metadata metadata;
    protected final Authentication authentication;
    protected final UserSessionSource userSessionSource;

    public BpmActivitiListener() {
        processRepositoryManager = AppBeans.get(ProcessRepositoryManager.class);
        processRuntimeManager = AppBeans.get(ProcessRuntimeManager.class);
        persistence = AppBeans.get(Persistence.class);
        timeSource = AppBeans.get(TimeSource.class);
        metadata = AppBeans.get(Metadata.class);
        authentication = AppBeans.get(Authentication.class);
        userSessionSource = AppBeans.get(UserSessionSource.class);
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
            case TASK_CREATED:
                TaskEntity task = (TaskEntity) ((ActivitiEntityEvent)event).getEntity();
                UUID bpmProcTaskId = (UUID) task.getVariableLocal("bpmProcTaskId");
                // bpmProcTaskId is not set in case of group task that doesn't have a concrete assignee
                // but only potential ones. In other cases TASK_ASSIGNED event will be fired first and the variable
                // will be already set
                if (bpmProcTaskId == null) {
                    ProcTask procTask = processRuntimeManager.createNotAssignedProcTask(task);
                    task.setVariableLocal("bpmProcTaskId", procTask.getId());
                }
                break;
            case TASK_ASSIGNED:
                task = (TaskEntity) ((ActivitiEntityEvent)event).getEntity();
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
            case JOB_EXECUTION_SUCCESS:
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
