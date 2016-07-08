/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component(ProcessMigrator.NAME)
public class ProcessMigratorBean implements ProcessMigrator {

    private final Logger log = LoggerFactory.getLogger(ProcessMigratorBean.class);

    @Inject
    protected ManagementService managementService;

    @Inject
    protected RuntimeService runtimeService;

    @Inject
    protected Persistence persistence;

    @Override
    public void migrate(ProcessDefinition actProcessDefinition) {
        List<ProcessInstance> actProcessInstances = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(actProcessDefinition.getKey())
                .list();
        if (!isMigrationPossible(actProcessDefinition, actProcessInstances)) {
            throw new BpmException("Process migration is not possible");
        }
        for (ProcessInstance pi : actProcessInstances) {
            managementService.executeCommand(new SetProcessDefinitionVersionCmd(pi.getId(), actProcessDefinition.getVersion()));
        }
    }

    @Override
    public void migrateProcTasks(ProcDefinition procDefinition, String actProcessDefinitionId) {
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            int updatedCount = em.createQuery("update bpm$ProcTask pt set pt.actProcessDefinitionId = :actProcessDefinitionId " +
                    "where pt.procInstance.procDefinition.id = :procDefinition")
                    .setParameter("actProcessDefinitionId", actProcessDefinitionId)
                    .setParameter("procDefinition", procDefinition)
                    .executeUpdate();
            log.debug(updatedCount + " procTasks was update during process migration");
            tx.commit();
        } finally {
            tx.end();
        }
    }

    protected boolean isMigrationPossible(ProcessDefinition actProcessDefinition, List<ProcessInstance> actProcessInstances) {
        //activiti does this check by itself. If new process doesn't contain a task with id that is absent in old
        //processes then an ActivitiException is thrown
        //todo gorbunkov handle this activiti exception and display more readable message
        return true;
    }
}