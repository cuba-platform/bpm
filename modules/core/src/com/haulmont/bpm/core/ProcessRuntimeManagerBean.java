/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.*;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.juel.ExpressionFactoryImpl;
import org.activiti.engine.impl.juel.SimpleContext;
import org.activiti.engine.impl.juel.TreeValueExpression;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

@Component(ProcessRuntimeManager.NAME)
public class ProcessRuntimeManagerBean implements ProcessRuntimeManager {

    @Inject
    protected RuntimeService runtimeService;

    @Inject
    protected TaskService taskService;

    @Inject
    protected Persistence persistence;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ExtensionElementsManager extensionElementsManager;

    protected static final Logger log = LoggerFactory.getLogger(ProcessRuntimeManagerBean.class);

    @Override
    public ProcInstance startProcess(ProcInstance procInstance, String comment, @Nullable Map<String, Object> variables) {
        if (PersistenceHelper.isNew(procInstance)) {
            throw new IllegalArgumentException("procInstance entity should be persisted");
        }

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            procInstance = em.reload(procInstance, "procInstance-start");
            if (procInstance == null) {
                throw new BpmException("Cannot start process. ProcInstance not found in database.");
            }
            if (procInstance.getProcDefinition() == null) {
                throw new BpmException("Cannot start process. ProcDefinition property is null.");
            }
            if (BooleanUtils.isTrue(procInstance.getActive())) {
                throw new BpmException("Cannot start process. ProcessInstance is already active.");
            }
            if (!procInstance.getProcDefinition().getActive()) {
                throw new BpmException("Cannot start process. Process definition is not active.");
            }

            if (variables == null)
                variables = new HashMap<>();
            variables.put("bpmProcInstanceId", procInstance.getId());
            if (!Strings.isNullOrEmpty(procInstance.getEntityName())) {
                variables.put("entityName", procInstance.getEntityName());
            }
            if (procInstance.getEntityId() != null) {
                variables.put("entityId", procInstance.getEntityId());
            }

            ProcessInstance activitiProcessInstance = runtimeService.startProcessInstanceById(procInstance.getProcDefinition().getActId(), variables);

            procInstance.setActProcessInstanceId(activitiProcessInstance.getProcessInstanceId());
            procInstance.setStartComment(comment);
            procInstance.setActive(true);
            procInstance.setStartDate(timeSource.currentTimestamp());
            procInstance.setStartedBy(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
            procInstance = em.merge(procInstance);

            tx.commit();
            return procInstance;
        } finally {
            tx.end();
        }
    }

    @Override
    public ProcInstance cancelProcess(ProcInstance procInstance, String comment) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            runtimeService.deleteProcessInstance(procInstance.getActProcessInstanceId(), comment);
            procInstance.setCancelled(true);
            procInstance.setActive(false);
            procInstance.setEndDate(timeSource.currentTimestamp());
            procInstance.setCancelComment(comment);

            TypedQuery<ProcTask> query = em.createQuery("select a from bpm$ProcTask a where a.procInstance.id = :procInstance " +
                    "and a.endDate is null", ProcTask.class);
            query.setParameter("procInstance", procInstance);
            List<ProcTask> procTasks = query.getResultList();

            for (ProcTask procTask : procTasks) {
                procTask.setEndDate(timeSource.currentTimestamp());
                procTask.setCancelled(true);
            }

            em.merge(procInstance);
            tx.commit();
            return procInstance;
        } finally {
            tx.end();
        }
    }

    @Override
    public String getSingleTaskAssignee(UUID procInstanceId, String procRoleCode) {
        List<String> logins = getTaskAssigneeList(procInstanceId, procRoleCode);
        if (logins.isEmpty()) {
            throw new BpmException("No actor found for procRole " + procRoleCode);
        }
        if (logins.size() > 1) {
            throw new BpmException("Multiple actors found for procRole " + procRoleCode);
        }
        return logins.get(0);
    }

    @Override
    public List<String> getTaskAssigneeList(UUID procInstanceId, String procRoleCode) {
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<UUID> query = em.createQuery("select pa.user.id from bpm$ProcActor pa " +
                    "where pa.procInstance.id = :procInstanceId and pa.procRole.code = :procRoleCode " +
                    "order by pa.order", UUID.class);
            query.setParameter("procInstanceId", procInstanceId);
            query.setParameter("procRoleCode", procRoleCode);
            List<UUID> queryResultList = query.getResultList();
            List<String> methodResult = new ArrayList<>();
            for (UUID userId : queryResultList) {
                methodResult.add(userId.toString());
            }
            tx.commit();
            return methodResult;
        } finally {
            tx.end();
        }
    }

    @Override
    public void completeProcTask(ProcTask procTask, String outcome, String comment, @Nullable Map<String, Object> processVariables) {
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            procTask = em.reload(procTask, "procTask-complete");
            if (procTask.getEndDate() != null) {
                throw new BpmException("procTask " + procTask.getId() + " already completed");
            }

            procTask.setEndDate(timeSource.currentTimestamp());
            procTask.setOutcome(outcome);
            procTask.setComment(comment);

            if (processVariables != null) {
                for (Map.Entry<String, Object> entry : processVariables.entrySet()) {
                    runtimeService.setVariable(procTask.getActExecutionId(), entry.getKey(), entry.getValue());
                }
            }

            //execution local variable 'outcome' can be used in non-multi-instance user tasks
            runtimeService.setVariableLocal(procTask.getActExecutionId(), "outcome", outcome);

            //execution variable '<taskName>_result' can be used after multi-instance tasks. It holds outcomes for all task actors
            String variableName = createResultVariableName(procTask.getActTaskDefinitionKey());
            ProcTaskResult taskResult = (ProcTaskResult) runtimeService.getVariable(procTask.getActExecutionId(), variableName);
            if (taskResult == null)
                taskResult = new ProcTaskResult();
            taskResult.addOutcome(outcome, procTask.getProcActor().getUser().getId());
            runtimeService.setVariable(procTask.getActExecutionId(), variableName, taskResult);
            taskService.complete(procTask.getActTaskId());

            tx.commit();
        } finally {
            tx.end();
        }
    }

    protected String createResultVariableName(String actTaskDefinitionKey) {
        return actTaskDefinitionKey.replace("-", "") + "_result";
    }

    @Override
    public void completeProcTaskOnTimer(UUID procTaskId, String outcome) {
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            ProcTask procTask = em.find(ProcTask.class, procTaskId, "procTask-complete");

            if (procTask == null) {
                throw new BpmException("Cannot complete procTask. ProcTask with id " + procTaskId + " not found");
            }

            if (procTask.getEndDate() != null) {
                throw new BpmException("procTask " + procTask.getId() + " already completed");
            }

            procTask.setEndDate(timeSource.currentTimestamp());
            procTask.setOutcome(outcome);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Override
    public void claimProcTask(ProcTask procTask, User user) {
        Transaction tx = persistence.getTransaction();
        try {
            taskService.claim(procTask.getActTaskId(), user.getId().toString());
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Override
    public long getActiveProcInstanceCount(ProcDefinition procDefinition) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Long count = (Long) em.createQuery("select count(pi) from bpm$ProcInstance pi where pi.procDefinition.id = :procDefinition and pi.active = true")
                    .setParameter("procDefinition", procDefinition)
                    .getSingleResult();
            tx.commit();
            return count;
        } finally {
            tx.end();
        }
    }

    @Override
    public Object evaluateExpression(String expression, String actExecutionId) {
        ExpressionFactoryImpl expressionFactory = new ExpressionFactoryImpl();
        SimpleContext simpleContext = new SimpleContext();

        Map<String, Object> variables = runtimeService.getVariables(actExecutionId);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            simpleContext.setVariable(entry.getKey(), expressionFactory.createValueExpression(entry.getValue(), entry.getValue().getClass()));
        }

        TreeValueExpression valueExpression = expressionFactory.createValueExpression(simpleContext, expression, Object.class);
        return valueExpression.getValue(simpleContext);
    }

    @Override
    public ProcTask createProcTask(TaskEntity actTask) {
        String assignee = actTask.getAssignee();
        if (Strings.isNullOrEmpty(assignee))
            throw new BpmException("No assignee defined for task " + actTask.getTaskDefinitionKey() + " with id = " + actTask.getId());

        UUID bpmProcInstanceId = (UUID) actTask.getVariable("bpmProcInstanceId");
        if (bpmProcInstanceId == null)
            throw new BpmException("No 'bpmProcInstanceId' process variable defined for activiti process " + actTask.getProcessInstanceId());
        EntityManager em = persistence.getEntityManager();

        ProcInstance procInstance = em.find(ProcInstance.class, bpmProcInstanceId);
        if (procInstance == null)
            throw new BpmException("Process instance with id " + bpmProcInstanceId + " not found");

        String roleCode = extensionElementsManager.getTaskProcRole(actTask.getProcessDefinitionId(), actTask.getTaskDefinitionKey());
        if (Strings.isNullOrEmpty(roleCode))
            throw new BpmException("ProcRole code for task " + actTask.getTaskDefinitionKey() + " not defined");
        ProcActor procActor = findProcActor(bpmProcInstanceId, roleCode, UUID.fromString(assignee));
        if (procActor == null)
            throw new BpmException("ProcActor " + roleCode + " not defined");

        Metadata metadata = AppBeans.get(Metadata.class);
        ProcTask procTask = metadata.create(ProcTask.class);
        procTask.setProcActor(procActor);
        procTask.setProcInstance(procInstance);
        procTask.setActExecutionId(actTask.getExecutionId());
        procTask.setName(actTask.getName());
        procTask.setActTaskDefinitionKey(actTask.getTaskDefinitionKey());
        procTask.setActTaskId(actTask.getId());
        procTask.setStartDate(AppBeans.get(TimeSource.class).currentTimestamp());
        procTask.setActProcessDefinitionId(actTask.getProcessDefinitionId());
        em.persist(procTask);

        //reset variable '<taskName>_result'
        String variableName = createResultVariableName(actTask.getTaskDefinitionKey());
        ProcTaskResult taskResult = new ProcTaskResult();
        runtimeService.setVariable(procTask.getActExecutionId(), variableName, taskResult);

        return procTask;
    }

    @Override
    public void assignProcTask(TaskEntity actTask) {
        UUID bpmProcTaskId = (UUID) actTask.getVariableLocal("bpmProcTaskId");

        EntityManager em = persistence.getEntityManager();
        ProcTask procTask = em.find(ProcTask.class, bpmProcTaskId);
        if (procTask == null) {
            throw new BpmException("Cannot assign procTask. ProcTask not found in database.");
        }

        UUID bpmProcInstanceId = (UUID) actTask.getVariable("bpmProcInstanceId");
        if (bpmProcInstanceId == null)
            throw new BpmException("No 'bpmProcInstanceId' process variable defined for activiti process " + actTask.getProcessInstanceId());

        ProcInstance procInstance = em.find(ProcInstance.class, bpmProcInstanceId);
        if (procInstance == null)
            throw new BpmException("Process instance with id " + bpmProcInstanceId + " not found");

        String roleCode = extensionElementsManager.getTaskProcRole(actTask.getProcessDefinitionId(), actTask.getTaskDefinitionKey());
        if (Strings.isNullOrEmpty(roleCode))
            throw new BpmException("Role code variable for task " + actTask.getTaskDefinitionKey() + " not defined");

        ProcActor procActor = findProcActor(bpmProcInstanceId, roleCode, UUID.fromString(actTask.getAssignee()));
        if (procActor == null) {
            User assigneeUser = em.find(User.class, UUID.fromString(actTask.getAssignee()));
            procActor = metadata.create(ProcActor.class);
            procActor.setProcInstance(procInstance);
            procActor.setProcRole(findProcRole(roleCode));
            procActor.setUser(assigneeUser);
            em.persist(procActor);
        }

        procTask.setProcActor(procActor);
        procTask.setClaimDate(timeSource.currentTimestamp());
    }

    @Override
    public ProcTask createNotAssignedProcTask(TaskEntity actTask) {
        Set<User> candidateUsers = getCandidateUsers(actTask);

        UUID bpmProcInstanceId = (UUID) actTask.getVariable("bpmProcInstanceId");
        if (bpmProcInstanceId == null)
            throw new BpmException("No 'bpmProcInstanceId' process variable defined for activiti process " + actTask.getProcessInstanceId());
        EntityManager em = persistence.getEntityManager();

        ProcInstance procInstance = em.find(ProcInstance.class, bpmProcInstanceId);
        if (procInstance == null)
            throw new BpmException("Process instance with id " + bpmProcInstanceId + " not found");

        Metadata metadata = AppBeans.get(Metadata.class);
        ProcTask procTask = metadata.create(ProcTask.class);
        procTask.setProcInstance(procInstance);
        procTask.setActExecutionId(actTask.getExecutionId());
        procTask.setName(actTask.getName());
        procTask.setActTaskId(actTask.getId());
        procTask.setActProcessDefinitionId(actTask.getProcessDefinitionId());
        procTask.setActTaskDefinitionKey(actTask.getTaskDefinitionKey());
        procTask.setStartDate(AppBeans.get(TimeSource.class).currentTimestamp());
        procTask.setCandidateUsers(candidateUsers);
        em.persist(procTask);

        //reset variable '<taskName>_result'
        String variableName = createResultVariableName(actTask.getTaskDefinitionKey());
        ProcTaskResult taskResult = new ProcTaskResult();
        runtimeService.setVariable(procTask.getActExecutionId(), variableName, taskResult);

        return procTask;
    }

    @Nullable
    protected ProcActor findProcActor(UUID procInstanceId, String procRoleCode, UUID userId) {
        Transaction tx = persistence.getTransaction();
        ProcActor procActor;
        try {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<ProcActor> query = em.createQuery("select pa from bpm$ProcActor pa " +
                    "where pa.procInstance.id = :procInstanceId " +
                    "and pa.procRole.code = :procRoleCode " +
                    "and pa.user.id = :userId", ProcActor.class);
            query.setViewName("procActor-procTaskCreation");
            query.setParameter("procInstanceId", procInstanceId);
            query.setParameter("procRoleCode", procRoleCode);
            query.setParameter("userId", userId);
            procActor = query.getFirstResult();
            tx.commit();
        } finally {
            tx.end();
        }
        return procActor;
    }

    @Nullable
    protected ProcRole findProcRole(String roleCode) {
        EntityManager em = persistence.getEntityManager();
        return (ProcRole) em.createQuery("select pr from bpm$ProcRole pr where pr.code = :code")
                .setParameter("code", roleCode)
                .getFirstResult();
    }

    protected Set<User> getCandidateUsers(TaskEntity task) {
        Set<User> candidateUsers = new HashSet<>();
        EntityManager em = persistence.getEntityManager();
        Set<IdentityLink> candidates = task.getCandidates();
        for (IdentityLink candidate : candidates) {
            User user = em.find(User.class, UUID.fromString(candidate.getUserId()));
            if (user != null) {
                candidateUsers.add(user);
            } else {
                log.warn("ProcTask candidate user with id " + candidate.getUserId() + " not found");
            }
        }
        return candidateUsers;
    }
}