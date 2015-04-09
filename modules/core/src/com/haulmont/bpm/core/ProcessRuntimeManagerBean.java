/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcActor;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.juel.ExpressionFactoryImpl;
import org.activiti.engine.impl.juel.SimpleContext;
import org.activiti.engine.impl.juel.TreeValueExpression;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.SpringProcessEngineConfiguration;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author gorbunkov
 * @version $Id$
 */
@ManagedBean(ProcessRuntimeManager.NAME)
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

    @Override
    public ProcInstance startProcess(ProcInstance procInstance, String comment, Map<String, Object> variables) {
        if (PersistenceHelper.isNew(procInstance)) {
            throw new IllegalArgumentException("procInstance entity should be persisted");
        }

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            procInstance = em.reload(procInstance, "procInstance-start");
            if (procInstance == null) {
                throw new BpmException("Cannot start process. ProcInstance has been deleted.");
            }
            if (procInstance.getProcDefinition() == null) {
                throw new BpmException("Cannot start process. ProcDefinition property is null.");
            }
            if (!procInstance.getProcDefinition().getActive()) {
                throw new BpmException("Cannot start process. Process definition is not active");
            }

            _startProcess(procInstance, comment, variables);

            procInstance = em.merge(procInstance);
            tx.commit();
            return procInstance;
        } finally {
            tx.end();
        }
    }

//    @Override
//    public ProcInstance startProcess(ProcDefinition procDefinition, String comment, Map<String, Object> variables) {
//        Transaction tx = persistence.createTransaction();
//        try {
//            EntityManager em = persistence.getEntityManager();
//            ProcInstance procInstance = metadata.create(ProcInstance.class);
//            procInstance.setProcDefinition(procDefinition);
//
//            _startProcess(procInstance, null, variables);
//
//            em.persist(procInstance);
//            tx.commit();
//            return procInstance;
//        } finally {
//            tx.end();
//        }
//    }

    protected void _startProcess(ProcInstance procInstance, String comment, Map<String, Object> variables) {
        variables.put("bpmProcInstanceId", procInstance.getId());
        ProcessInstance activitiProcessInstance = runtimeService.startProcessInstanceById(procInstance.getProcDefinition().getActId(), variables);
        procInstance.setActProcessInstanceId(activitiProcessInstance.getProcessInstanceId());
        procInstance.setStartComment(comment);
        procInstance.setActive(true);
        procInstance.setStartDate(timeSource.currentTimestamp());
        procInstance.setStartedBy(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
    }

    @Override
    public ProcInstance cancelProcess(ProcInstance procInstance, String comment) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            runtimeService.deleteProcessInstance(procInstance.getActProcessInstanceId(), comment);
            procInstance.setCanceled(true);
            procInstance.setActive(false);
            procInstance.setEndDate(timeSource.currentTimestamp());
            procInstance.setCancelComment(comment);

            TypedQuery<ProcTask> query = em.createQuery("select a from bpm$ProcTask a where a.procInstance.id = :procInstance " +
                    "and a.endDate is null", ProcTask.class);
            query.setParameter("procInstance", procInstance);
            List<ProcTask> procTasks = query.getResultList();

            for (ProcTask procTask : procTasks) {
                procTask.setEndDate(timeSource.currentTimestamp());
                procTask.setCanceled(true);
            }

            em.merge(procInstance);
            tx.commit();
            return procInstance;
        } finally {
            tx.end();
        }
    }

    @Override
    @Nullable
    public ProcActor findProcActor(UUID procInstanceId, String procRoleCode, UUID userId) {
        Transaction tx = persistence.getTransaction();
        ProcActor procActor;
        try {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<ProcActor> query = em.createQuery("select pa from bpm$ProcActor pa " +
                    "where pa.procInstance.id = :procInstanceId and pa.procRole.code = :procRoleCode " +
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
    public void completeProcTask(ProcTask procTask, String outcome, String comment) {
        completeProcTask(procTask, outcome, comment, null);
    }

    @Override
    public void completeProcTask(ProcTask procTask, String outcome, String comment, @Nullable Map<String, Object> processVariables) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            procTask = em.reload(procTask, "procTask-complete");
            if (procTask.getEndDate() != null) {
                throw new BpmException("procTask " + procTask.getId() + " already completed");
            }
            runtimeService.setVariableLocal(procTask.getActExecutionId(), "outcome", outcome);

            ProcTaskResult taskResult = (ProcTaskResult) runtimeService.getVariable(procTask.getActExecutionId(), procTask.getName() + "_result");
            if (taskResult == null) taskResult = new ProcTaskResult();
            taskResult.addOutcome(outcome, procTask.getProcActor().getUser().getLogin());
            runtimeService.setVariable(procTask.getActExecutionId(), procTask.getName() + "_result", taskResult);

            procTask.setEndDate(timeSource.currentTimestamp());
            procTask.setOutcome(outcome);
            procTask.setComment(comment);

            if (processVariables != null) {
                for (Map.Entry<String, Object> entry : processVariables.entrySet()) {
                    runtimeService.setVariable(procTask.getActExecutionId(), entry.getKey(), entry.getValue());
                }
            }

            taskService.complete(procTask.getActTaskId());

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Override
    public void claimProcTask(ProcTask procTask, User user) {
        Transaction tx = persistence.createTransaction();
        try {
            taskService.claim(procTask.getActTaskId(), user.getId().toString());
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Override
    public long getActiveProcessesCount(ProcDefinition procDefinition) {
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
}
