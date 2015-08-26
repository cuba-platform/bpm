/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.testsupport;

import com.haulmont.bali.db.QueryRunner;
import com.haulmont.bpm.core.ExtensionElementsManager;
import com.haulmont.bpm.core.ProcessFormManager;
import com.haulmont.bpm.core.ProcessRepositoryManager;
import com.haulmont.bpm.core.ProcessRuntimeManager;
import com.haulmont.cuba.core.CubaTestCase;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.testsupport.TestContext;
import com.haulmont.cuba.testsupport.TestDataSource;
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

/**
 * @author gorbunkov
 * @version $Id$
 */
public class BpmTestCase extends CubaTestCase {

    protected RepositoryService repositoryService;
    protected TaskService taskService;
    protected RuntimeService runtimeService;

    protected ProcessRepositoryManager processRepositoryManager;
    protected ProcessRuntimeManager processRuntimeManager;
    protected ExtensionElementsManager extensionElementsManager;
    protected ProcessFormManager processFormManager;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        repositoryService = AppBeans.get(RepositoryService.class);
        taskService = AppBeans.get(TaskService.class);
        runtimeService = AppBeans.get(RuntimeService.class);
        processRepositoryManager = AppBeans.get(ProcessRepositoryManager.class);
        processRuntimeManager = AppBeans.get(ProcessRuntimeManager.class);
        extensionElementsManager = AppBeans.get(ExtensionElementsManager.class);
        processFormManager = AppBeans.get(ProcessFormManager.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanUpDatabase();
    }

    protected void cleanUpDatabase() {
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
        QueryRunner runner = new QueryRunner(persistence.getDataSource());
        try {
            runner.update(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void cleanUpUsersTable() {
        String sql = "delete from SEC_USER where login <> 'admin'";
        QueryRunner runner = new QueryRunner(persistence.getDataSource());
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

    @Override
    protected void initDataSources() throws Exception {
        Class.forName("org.postgresql.Driver");
        TestDataSource ds = new TestDataSource("jdbc:postgresql://localhost/bpm_test", "root", "root");
        TestContext.getInstance().bind("java:comp/env/jdbc/CubaDS", ds);
    }

    @Override
    protected String getTestSpringConfig() {
        return "test-bpm-spring.xml";
    }

    @Override
    protected List<String> getTestAppProperties() {
        String[] files = {
                "cuba-app.properties",
                "bpm-app.properties",
                "test-app.properties",
        };
        return Arrays.asList(files);
    }
}
