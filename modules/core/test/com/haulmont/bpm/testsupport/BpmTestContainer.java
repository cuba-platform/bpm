/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.testsupport;

import com.haulmont.bali.db.QueryRunner;
import com.haulmont.bpm.core.ExtensionElementsManager;
import com.haulmont.bpm.core.ProcessFormManager;
import com.haulmont.bpm.core.ProcessRepositoryManager;
import com.haulmont.bpm.core.ProcessRuntimeManager;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.testsupport.TestContainer;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class BpmTestContainer extends TestContainer {

    private RepositoryService repositoryService;
    private TaskService taskService;
    private RuntimeService runtimeService;

    private ProcessRepositoryManager processRepositoryManager;
    private ProcessRuntimeManager processRuntimeManager;
    private ExtensionElementsManager extensionElementsManager;
    private ProcessFormManager processFormManager;

    public BpmTestContainer() {
        super();
        appComponents = Arrays.asList(
                "com.haulmont.cuba"
        );
        appPropertiesFiles = Arrays.asList(
                "com/haulmont/cuba/app.properties",
                "com/haulmont/bpm/app.properties",
                "com/haulmont/cuba/testsupport/test-app.properties",
                "com/haulmont/bpm/test-app.properties");

        dbDriver = "org.postgresql.Driver";
        dbUrl = "jdbc:postgresql://localhost/bpm_test";
        dbUser = "root";
        dbPassword = "root";
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        initProperties();
    }

    protected void initProperties() {
        repositoryService = AppBeans.get(RepositoryService.class);
        taskService = AppBeans.get(TaskService.class);
        runtimeService = AppBeans.get(RuntimeService.class);
        processRepositoryManager = AppBeans.get(ProcessRepositoryManager.class);
        processRuntimeManager = AppBeans.get(ProcessRuntimeManager.class);
        extensionElementsManager = AppBeans.get(ExtensionElementsManager.class);
        processFormManager = AppBeans.get(ProcessFormManager.class);
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public RuntimeService getRuntimeService() {
        return runtimeService;
    }

    public ProcessRepositoryManager getProcessRepositoryManager() {
        return processRepositoryManager;
    }

    public ProcessRuntimeManager getProcessRuntimeManager() {
        return processRuntimeManager;
    }

    public ExtensionElementsManager getExtensionElementsManager() {
        return extensionElementsManager;
    }

    public ProcessFormManager getProcessFormManager() {
        return processFormManager;
    }

    public void cleanUpDatabase() {
        cleanUpTable("BPM_PROC_TASK_USER_LINK");
        cleanUpTable("BPM_PROC_TASK");
        cleanUpTable("BPM_PROC_ACTOR");
        cleanUpTable("BPM_PROC_INSTANCE");
        cleanUpTable("BPM_PROC_ROLE");
        cleanUpTable("BPM_PROC_DEFINITION");
        cleanUpTable("BPM_PROC_MODEL");
        cleanUpUsersTable();
        cleanUpActivitiEngineDatabase();
    }

    protected void cleanUpTable(String tableName) {
        String sql = "delete from " + tableName;
        QueryRunner runner = new QueryRunner(persistence().getDataSource());
        try {
            runner.update(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void cleanUpUsersTable() {
        String sql = "delete from SEC_USER where login <> 'admin'";
        QueryRunner runner = new QueryRunner(persistence().getDataSource());
        try {
            runner.update(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void cleanUpActivitiEngineDatabase() {
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
        for (ProcessInstance processInstance : processInstances) {
            runtimeService.deleteProcessInstance(processInstance.getId(), "test database cleanup");
        }

        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        for (ProcessDefinition processDefinition : processDefinitions) {
            repositoryService.deleteDeployment(processDefinition.getDeploymentId());
        }

        List<Model> models = repositoryService.createModelQuery().list();
        for (Model model : models) {
            repositoryService.deleteModel(model.getId());
        }

        List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId());
        }
    }
}
