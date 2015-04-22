/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.exception.BpmException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.List;

/**
 * @author gorbunkov
 * @version $Id$
 */
@ManagedBean(ProcessMigrator.NAME)
public class ProcessMigratorBean implements ProcessMigrator {

    @Inject
    protected ManagementService managementService;

    @Inject
    protected RuntimeService runtimeService;

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

    protected boolean isMigrationPossible(ProcessDefinition actProcessDefinition, List<ProcessInstance> actProcessInstances) {
//        Set<String> checkedActivities = new HashSet<>();
//        BpmnModel bpmnModel = repositoryService.getBpmnModel(actProcessDefinition.getId());
//        for (ProcessInstance pi : actProcessInstances) {
//            List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).list();
//            for (Execution execution : executions) {
//                if (!)
//            }
//        }
        return true;
    }
}
