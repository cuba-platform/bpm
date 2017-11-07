/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm

import com.haulmont.bpm.entity.ProcDefinition
import com.haulmont.bpm.entity.ProcInstance
import com.haulmont.bpm.testsupport.BpmTestContainer
import com.haulmont.cuba.core.Transaction
import org.junit.After
import org.junit.ClassRule
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 *
 */
class ProcessMigrationTest {

    static final String PROCESS_PATH_1 = "com/haulmont/bpm/process/migrationTest1.bpmn20.xml"
    static final String PROCESS_PATH_2 = "com/haulmont/bpm/process/migrationTest2.bpmn20.xml"

    @ClassRule
    public static BpmTestContainer cont = new BpmTestContainer();

    @After
    public void after() {
        cont.cleanUpDatabase();
    }

    @Test
    @SuppressWarnings("GroovyAssignabilityCheck")
    void testProcessMigration() throws Exception {

        ProcDefinition procDefinition = cont.processRepositoryManager.deployProcessFromPath(PROCESS_PATH_1, null, null)

        ProcInstance procInstance = new ProcInstance(procDefinition: procDefinition)
        cont.persistence().createTransaction().execute( { em ->
            em.persist(procInstance)
        } as Transaction.Runnable)

        procInstance = cont.processRuntimeManager.startProcess(procInstance, '', null)

        cont.processRepositoryManager.deployProcessFromPath(PROCESS_PATH_2, procDefinition, null)

        def task = cont.taskService.createTaskQuery().processInstanceId(procInstance.actProcessInstanceId).singleResult()

        cont.taskService.complete(task.id)

        task = cont.taskService.createTaskQuery().processInstanceId(procInstance.actProcessInstanceId).singleResult()
        assertEquals('newScanning', task.taskDefinitionKey)
    }
}
