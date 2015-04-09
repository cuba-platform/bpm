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

    public BpmActivitiListener() {
        processRepositoryManager = AppBeans.get(ProcessRepositoryManager.class);
        processRuntimeManager = AppBeans.get(ProcessRuntimeManager.class);
        persistence = AppBeans.get(Persistence.class);
        timeSource = AppBeans.get(TimeSource.class);
        metadata = AppBeans.get(Metadata.class);
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
//            case ACTIVITY_STARTED:
//                if (event instanceof ActivitiActivityEvent && "userTask".equals(((ActivitiActivityEvent) event).getActivityType())) {
//                    onTaskCreated((ActivitiActivityEvent) event);
//                }
//                break;
            case TASK_CREATED:
                TaskEntity task = (TaskEntity) ((ActivitiEntityEvent)event).getEntity();
                UUID bpmProcTaskId = (UUID) task.getVariableLocal("bpmProcTaskId");
                // bpmProcTaskId is not set in case of group task that doesn't have a concrete assignee
                // but only potential ones. In other cases TASK_ASSIGNED event will be fired first and the variable
                // will be already set
                if (bpmProcTaskId == null) {
                    ProcTask procTask = createNotAssignedProcTask(task);
                    task.setVariableLocal("bpmProcTaskId", procTask.getId());
                }
                break;
            case TASK_ASSIGNED:
                task = (TaskEntity) ((ActivitiEntityEvent)event).getEntity();
                bpmProcTaskId = (UUID) task.getVariableLocal("bpmProcTaskId");
                // bpmProcTaskId will be not null if the task is claimed
                if (bpmProcTaskId == null) {
                    ProcTask procTask = createProcTask(task);
                    task.setVariableLocal("bpmProcTaskId", procTask.getId());
                } else {
                    assignProcTask(task);
                }
                break;
            case PROCESS_COMPLETED:
                onProcessCompleted(event);
                break;
//            case PROCESS_CANCELLED:
//                onProcessCancelled(event);
        }
    }

//    private void onTaskCreated(ActivitiEntityEvent event) {
//        TaskEntity task = (TaskEntity) event.getEntity();
//        Map<String, String> params = processRepositoryManager.getFlowElementParams(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
//        for (Map.Entry<String, String> entry : params.entrySet()) {
//            task.getExecution().setVariableLocal(entry.getKey(), entry.getValue());
//        }
//    }

//    private void onTaskCreated(ActivitiActivityEvent event) {
//        Map<String, String> params = processRepositoryManager.getFlowElementParams(event.getProcessDefinitionId(), event.getActivityId());
//        for (Map.Entry<String, String> entry : params.entrySet()) {
//            event.getEngineServices().getTaskService().setVariableLocal(event.getExecutionId(), entry.getKey(), entry.getValue());
//        }
//    }
//

    protected ProcTask createProcTask(TaskEntity task) {
        String assignee = task.getAssignee();
        if (Strings.isNullOrEmpty(assignee))
            throw new BpmException("No assignee defined for task " + task.getTaskDefinitionKey() + " with id = " + task.getId());

        UUID bpmProcInstanceId = (UUID) task.getVariable("bpmProcInstanceId");
        if (bpmProcInstanceId == null)
            throw new BpmException("No 'bpmProcInstanceId' process variable defined for activiti process " + task.getProcessInstanceId());
        EntityManager em = persistence.getEntityManager();

        ProcInstance procInstance = em.find(ProcInstance.class, bpmProcInstanceId);
        if (procInstance == null)
            throw new BpmException("Process instance with id " + bpmProcInstanceId + " not found");

        String roleCode = (String) task.getExecution().getVariable(task.getTaskDefinitionKey() + "_role");
        if (Strings.isNullOrEmpty(roleCode))
            throw new BpmException("Role code for task " + task.getTaskDefinitionKey() + " not defined");
        ProcActor procActor = processRuntimeManager.findProcActor(bpmProcInstanceId, roleCode, UUID.fromString(assignee));
        if (procActor == null)
            throw new BpmException("ProcActor " + roleCode + " not defined");

        Metadata metadata = AppBeans.get(Metadata.class);
        ProcTask procTask = metadata.create(ProcTask.class);
        procTask.setProcActor(procActor);
        procTask.setProcInstance(procInstance);
        procTask.setActExecutionId(task.getExecutionId());
        procTask.setName(task.getTaskDefinitionKey());
        procTask.setActTaskId(task.getId());
        procTask.setStartDate(AppBeans.get(TimeSource.class).currentTimestamp());
        procTask.setActProcessDefinitionId(task.getProcessDefinitionId());
        em.persist(procTask);

        return procTask;
    }

    protected void assignProcTask(TaskEntity taskEntity) {
        UUID bpmProcTaskId = (UUID) taskEntity.getVariableLocal("bpmProcTaskId");

        EntityManager em = persistence.getEntityManager();
        ProcTask procTask = em.find(ProcTask.class, bpmProcTaskId);

        UUID bpmProcInstanceId = (UUID) taskEntity.getVariable("bpmProcInstanceId");
        if (bpmProcInstanceId == null)
            throw new BpmException("No 'bpmProcInstanceId' process variable defined for activiti process " + taskEntity.getProcessInstanceId());

        ProcInstance procInstance = em.find(ProcInstance.class, bpmProcInstanceId);
        if (procInstance == null)
            throw new BpmException("Process instance with id " + bpmProcInstanceId + " not found");

        String roleCode = (String) taskEntity.getExecution().getVariable(taskEntity.getTaskDefinitionKey() + "_role");
        ProcActor procActor = processRuntimeManager.findProcActor(bpmProcInstanceId, roleCode, UUID.fromString(taskEntity.getAssignee()));
        if (procActor == null) {
            User assigneeUser = em.find(User.class, UUID.fromString(taskEntity.getAssignee()));
            procActor = metadata.create(ProcActor.class);
            procActor.setProcInstance(procInstance);
            procActor.setProcRole(findProcRole(roleCode));
            procActor.setUser(assigneeUser);
            em.persist(procActor);
        }

        procTask.setProcActor(procActor);
        procTask.setClaimDate(timeSource.currentTimestamp());
    }

    protected ProcTask createNotAssignedProcTask(TaskEntity taskEntity) {
        Set<User> candidateUsers = getCandidateUsers(taskEntity);

        UUID bpmProcInstanceId = (UUID) taskEntity.getVariable("bpmProcInstanceId");
        if (bpmProcInstanceId == null)
            throw new BpmException("No 'bpmProcInstanceId' process variable defined for activiti process " + taskEntity.getProcessInstanceId());
        EntityManager em = persistence.getEntityManager();

        ProcInstance procInstance = em.find(ProcInstance.class, bpmProcInstanceId);
        if (procInstance == null)
            throw new BpmException("Process instance with id " + bpmProcInstanceId + " not found");

        Metadata metadata = AppBeans.get(Metadata.class);
        ProcTask procTask = metadata.create(ProcTask.class);
        procTask.setProcInstance(procInstance);
        procTask.setActExecutionId(taskEntity.getExecutionId());
        procTask.setName(taskEntity.getTaskDefinitionKey());
        procTask.setActTaskId(taskEntity.getId());
        procTask.setActProcessDefinitionId(taskEntity.getProcessDefinitionId());
        procTask.setStartDate(AppBeans.get(TimeSource.class).currentTimestamp());
        procTask.setCandidateUsers(candidateUsers);
        em.persist(procTask);

        return procTask;
    }

    protected Set<User> getCandidateUsers(TaskEntity task) {
        EntityManager em = persistence.getEntityManager();
        Set<IdentityLink> candidates = task.getCandidates();
        Set<User> candidateUsers = new HashSet<>();
        for (IdentityLink candidate : candidates) {
            User user = em.find(User.class, UUID.fromString(candidate.getUserId()));
            if (user != null)
                candidateUsers.add(user);
        }
        return candidateUsers;
    }

    protected ProcRole findProcRole(String roleCode) {
        EntityManager em = persistence.getEntityManager();
        return (ProcRole) em.createQuery("select pr from bpm$ProcRole pr where pr.code = :code")
                .setParameter("code", roleCode)
                .getFirstResult();
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
