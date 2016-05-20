/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm

import com.haulmont.bpm.entity.ProcDefinition
import com.haulmont.bpm.testsupport.BpmTestContainer
import org.junit.After
import org.junit.ClassRule
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 */
class ProcessDeploymentTest {

    static final String DEPLOYMENT_TEST_1_PATH = "com/haulmont/bpm/process/deploymentTest1.bpmn20.xml";
    static final String DEPLOYMENT_TEST_2_PATH = "com/haulmont/bpm/process/deploymentTest2.bpmn20.xml";
    static final String DEPLOYMENT_TEST_3_PATH = "com/haulmont/bpm/process/deploymentTest3.bpmn20.xml";

    @ClassRule
    public static BpmTestContainer cont = new BpmTestContainer();

    @After
    public void cleanUpDatabase() {
        cont.cleanUpDatabase();
    }

    @Test
    void deploymentOfProcessWithSimilarKey() {
        ProcDefinition procDefinition1 = cont.processRepositoryManager.deployProcessFromPath(DEPLOYMENT_TEST_1_PATH, null, null)
        ProcDefinition procDefinition2 = cont.processRepositoryManager.deployProcessFromPath(DEPLOYMENT_TEST_2_PATH, null, null)
        ProcDefinition procDefinition3 = cont.processRepositoryManager.deployProcessFromPath(DEPLOYMENT_TEST_3_PATH, null, null)

        assertEquals("bookScanning", procDefinition1.getCode());
        assertEquals("bookScanning-1", procDefinition2.getCode());
        assertEquals("bookScanning-2", procDefinition3.getCode());
    }
}
