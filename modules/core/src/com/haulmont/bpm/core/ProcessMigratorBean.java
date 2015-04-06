/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.log4j.spi.RepositorySelector;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Inject
    protected RepositoryService repositoryService;

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
