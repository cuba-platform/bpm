/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm

import com.haulmont.bpm.core.ProcessMessagesManager
import com.haulmont.bpm.core.ProcessVariablesManager
import com.haulmont.bpm.entity.ProcDefinition
import com.haulmont.bpm.entity.ProcInstance
import com.haulmont.bpm.entity.ProcRole
import com.haulmont.bpm.entity.ProcTask
import com.haulmont.bpm.testsupport.BpmTestContainer
import com.haulmont.bpm.testsupport.ObjectGraphBuilderProvider
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.security.entity.Group
import com.haulmont.cuba.security.entity.User
import org.junit.After
import org.junit.ClassRule
import org.junit.Test

import static junit.framework.Assert.assertNotNull
import static junit.framework.TestCase.assertNull
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse

/**
 *
 */
class ProcessRuntimeTest {

    static final String BASIC_PROCESS_PATH = "com/haulmont/bpm/process/testBasic.bpmn20.xml";
    static final String MULTI_INSTANCE_PARALLEL_PROCESS_PATH = "com/haulmont/bpm/process/testMultiInstanceParallel.bpmn20.xml";
    static final String MULTI_INSTANCE_SEQUENTIAL_PROCESS_PATH = "com/haulmont/bpm/process/testMultiInstanceSequential.bpmn20.xml";
    static final String CLAIM_TASK_PROCESS_PATH = "com/haulmont/bpm/process/testClaimTask.bpmn20.xml";
    static final String SCRIPT_TASK_PROCESS_PATH = "com/haulmont/bpm/process/testScriptTask.bpmn20.xml";
    static final String VARIABLES_API_PROCESS_PATH = "com/haulmont/bpm/process/testVariablesApi.bpmn20.xml";
    static final String MULTIPLE_ENTER_TO_TASK_PROCESS_PATH = "com/haulmont/bpm/process/testMutipleEnterToTask.bpmn20.xml";

    @ClassRule
    public static BpmTestContainer cont = new BpmTestContainer();

    @After
    public void cleanUpDatabase() {
        cont.cleanUpDatabase();
    }

    @Test
    void testBasic() {
        ProcDefinition procDefinition = cont.processRepositoryManager.deployProcessFromPath(BASIC_PROCESS_PATH, null, null)

        //check that procInstance object is created in bpm database and internal activiti database
        cont.persistence().createTransaction().execute( {em ->
            def reloadedProcDefinition = em.createQuery('select pd from bpm$ProcDefinition pd where pd.id = :id')
                    .setParameter('id', procDefinition.id)
                    .getFirstResult()
            assertNotNull(reloadedProcDefinition)

            def activitiProcessDefinitions = cont.repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(procDefinition.actId)
                    .list()
            assertEquals(1, activitiProcessDefinitions.size())
        } as Transaction.Runnable)

        assertEquals(2, procDefinition.procRoles.size())

        def managerProcRole = procDefinition.procRoles.find {it.code == 'manager'}
        def operatorProcRole = procDefinition.procRoles.find {it.code == 'operator'}

        assertNotNull(managerProcRole)
        assertEquals('manager', managerProcRole.code)
        assertEquals('Manager', managerProcRole.name)
        assertEquals(0, managerProcRole.order)

        assertNotNull(operatorProcRole)
        assertEquals('operator', operatorProcRole.code)
        assertEquals('Operator', operatorProcRole.name)
        assertEquals(1, operatorProcRole.order)

        ProcInstance newProcessInstance

        User johnDoeUser
        User marySmithUser

        //create test users and proc actors
        cont.persistence().createTransaction().execute( { em ->
            Group group = em.createQuery('select g from sec$Group g', Group.class).getFirstResult();

            johnDoeUser = cont.metadata().create(User.class)
            johnDoeUser.login = 'johndoe'
            johnDoeUser.group = group;
            em.persist(johnDoeUser)

            marySmithUser = cont.metadata().create(User.class)
            marySmithUser.login = 'marysmith'
            marySmithUser.group = group;
            em.persist(marySmithUser)

            def builder = ObjectGraphBuilderProvider.createBuilder(em)
            newProcessInstance = builder.procInstance(testId: 'procInstance1', procDefinition: procDefinition) {
                procActor(user: johnDoeUser, procRole: managerProcRole) {
                    procInstance(refId: 'procInstance1')
                }
                procActor(user: marySmithUser, procRole: operatorProcRole) {
                    procInstance(refId: 'procInstance1')
                }
            }
        } as Transaction.Runnable)

        newProcessInstance = cont.processRuntimeManager.startProcess(newProcessInstance, '', [:])

        //check that proctask was created for user with manager role
        ProcTask procTask
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            procTask = procTasks[0]
            assertNotNull(procTask)
            assertEquals(johnDoeUser.id, procTask.procActor.user.id)
        } as Transaction.Runnable)

        //test process forms
        Map procTaskForms = cont.processFormManager.getOutcomesWithForms(procTask)
        assertEquals(2, procTaskForms.size())

        def approveForm = procTaskForms['approve']
        assertEquals('standardProcessForm', approveForm.name)
        assertEquals(2, approveForm.params.size())
        assertEquals("true", approveForm.getParam('commentRequired').value)
        assertEquals("true", approveForm.getParam('attachmentsVisible').value)

        def rejectForm = procTaskForms['reject']
        assertEquals('someOtherProcessForm', rejectForm.name)
        assertEquals(0, rejectForm.params.size())

        def startForm = cont.processFormManager.getStartForm(procDefinition)
        assertEquals("startProcessForm", startForm.name)
        assertEquals(1, startForm.params.size())
        assertEquals("true", startForm.getParam('procActorsVisible').value)

        cont.processRuntimeManager.completeProcTask(procTask, 'approve', 'Scanning approved by manager', [:])

        cont.persistence().createTransaction().execute ({ em ->
            procTask = em.reload(procTask)
            assertNotNull(procTask)
            assertNotNull(procTask.endDate)
            assertEquals('approve', procTask.outcome)
            assertEquals('Scanning approved by manager', procTask.comment)
        } as Transaction.Runnable)

        //check that process went on a correct flow of execution
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            procTask = procTasks[0]
            assertNotNull(procTask)
            assertEquals('scanning', procTask.actTaskDefinitionKey)
            assertEquals('Scanning', procTask.name)
            assertEquals(marySmithUser.id, procTask.procActor.user.id)
        } as Transaction.Runnable)

        cont.processRuntimeManager.completeProcTask(procTask, 'complete', 'Scanning completed', [:])

        //check that process completed
        cont.persistence().createTransaction().execute( { em ->
            newProcessInstance = em.reload(newProcessInstance)
            assertFalse(newProcessInstance.active)
        } as Transaction.Runnable)
    }

    @Test
    void testMultiInstanceParallel() {
        ProcDefinition procDefinition = cont.processRepositoryManager.deployProcessFromPath(MULTI_INSTANCE_PARALLEL_PROCESS_PATH, null, null)
        ProcInstance newProcessInstance

        User johnDoeUser
        User marySmithUser
        User bobDylanUser

        ProcRole managerProcRole = procDefinition.procRoles.find {it.code == 'manager'}
        ProcRole operatorProcRole = procDefinition.procRoles.find {it.code == 'operator'}

        //create test users and proc actors
        cont.persistence().createTransaction().execute( { em ->
            Group group = em.createQuery('select g from sec$Group g', Group.class).getFirstResult();

            johnDoeUser = cont.metadata().create(User.class)
            johnDoeUser.login = 'johndoe'
            johnDoeUser.group = group
            em.persist(johnDoeUser)

            marySmithUser = cont.metadata().create(User.class)
            marySmithUser.login = 'marysmith'
            marySmithUser.group = group
            em.persist(marySmithUser)

            bobDylanUser = cont.metadata().create(User.class)
            bobDylanUser.login = 'bobdylan'
            bobDylanUser.group = group
            em.persist(bobDylanUser)

            def builder = ObjectGraphBuilderProvider.createBuilder(em)
            newProcessInstance = builder.procInstance(testId: 'procInstance1', procDefinition: procDefinition) {
                procActor(user: johnDoeUser, procRole: managerProcRole) {
                    procInstance(refId: 'procInstance1')
                }
                procActor(user: bobDylanUser, procRole: managerProcRole) {
                    procInstance(refId: 'procInstance1')
                }
                procActor(user: marySmithUser, procRole: operatorProcRole) {
                    procInstance(refId: 'procInstance1')
                }
            }
        } as Transaction.Runnable)

        newProcessInstance = cont.processRuntimeManager.startProcess(newProcessInstance, '', [:])


        ProcTask johnDoeProcTask
        ProcTask bobDylanProcTask

        //check that 2 process tasks created
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(2, procTasks.size())

            johnDoeProcTask = procTasks.find { it.procActor.user.id == johnDoeUser.id }
            bobDylanProcTask = procTasks.find { it.procActor.user.id == bobDylanUser.id }

            assertEquals('managerApproval', johnDoeProcTask.actTaskDefinitionKey)
            assertEquals('managerApproval', bobDylanProcTask.actTaskDefinitionKey)
        } as Transaction.Runnable)

        cont.processRuntimeManager.completeProcTask(johnDoeProcTask, 'approve', '', null)

        //check that one process task is still active
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            bobDylanProcTask = procTasks.find { it.procActor.user.id == bobDylanUser.id }
            assertEquals('managerApproval', bobDylanProcTask.actTaskDefinitionKey)
        } as Transaction.Runnable)

        cont.processRuntimeManager.completeProcTask(bobDylanProcTask, 'reject', '', null)

        //check that process went on correct execution flow and finished
        cont.persistence().createTransaction().execute( { em ->
            newProcessInstance = em.reload(newProcessInstance)
            assertFalse(newProcessInstance.active)
        } as Transaction.Runnable)
    }

    @Test
    void testMultiInstanceSequential() {
        ProcDefinition procDefinition = cont.processRepositoryManager.deployProcessFromPath(MULTI_INSTANCE_SEQUENTIAL_PROCESS_PATH, null, null)
        ProcInstance newProcessInstance

        User johnDoeUser
        User marySmithUser
        User bobDylanUser

        ProcRole managerProcRole = procDefinition.procRoles.find {it.code == 'manager'}
        ProcRole operatorProcRole = procDefinition.procRoles.find {it.code == 'operator'}

        //create test users and proc actors
        cont.persistence().createTransaction().execute( { em ->
            Group group = em.createQuery('select g from sec$Group g', Group.class).getFirstResult();

            johnDoeUser = cont.metadata().create(User.class)
            johnDoeUser.login = 'johndoe'
            johnDoeUser.group = group
            em.persist(johnDoeUser)

            marySmithUser = cont.metadata().create(User.class)
            marySmithUser.login = 'marysmith'
            marySmithUser.group = group
            em.persist(marySmithUser)

            bobDylanUser = cont.metadata().create(User.class)
            bobDylanUser.login = 'bobdylan'
            bobDylanUser.group = group
            em.persist(bobDylanUser)

            def builder = ObjectGraphBuilderProvider.createBuilder(em)
            newProcessInstance = builder.procInstance(testId: 'procInstance1', procDefinition: procDefinition) {
                procActor(user: johnDoeUser, procRole: managerProcRole, order: 0) {
                    procInstance(refId: 'procInstance1')
                }
                procActor(user: bobDylanUser, procRole: managerProcRole, order: 1) {
                    procInstance(refId: 'procInstance1')
                }
                procActor(user: marySmithUser, procRole: operatorProcRole) {
                    procInstance(refId: 'procInstance1')
                }
            }
        } as Transaction.Runnable)

        newProcessInstance = cont.processRuntimeManager.startProcess(newProcessInstance, '', [:])

        ProcTask johnDoeProcTask
        ProcTask bobDylanProcTask

        //check that process task for first manager created
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            johnDoeProcTask = procTasks.find { it.procActor.user.id == johnDoeUser.id }
            assertEquals('managerApproval', johnDoeProcTask.actTaskDefinitionKey)
        } as Transaction.Runnable)

        cont.processRuntimeManager.completeProcTask(johnDoeProcTask, 'approve', '', null)

        //check that process task for second manager created
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            bobDylanProcTask = procTasks.find { it.procActor.user.id == bobDylanUser.id }
            assertEquals('managerApproval', bobDylanProcTask.actTaskDefinitionKey)
        } as Transaction.Runnable)

        cont.processRuntimeManager.completeProcTask(bobDylanProcTask, 'reject', '', null)

        //check that process went on correct execution flow and finished
        cont.persistence().createTransaction().execute( { em ->
            newProcessInstance = em.reload(newProcessInstance)
            assertFalse(newProcessInstance.active)
        } as Transaction.Runnable)
    }

    @Test
    void testClaimTask() {
        ProcDefinition procDefinition = cont.processRepositoryManager.deployProcessFromPath(CLAIM_TASK_PROCESS_PATH, null, null)

        ProcInstance newProcessInstance
        User johnDoeUser
        User marySmithUser
        User bobDylanUser

        def managerProcRole = procDefinition.procRoles.find {it.code == 'manager'}
        def operatorProcRole = procDefinition.procRoles.find {it.code == 'operator'}

        cont.persistence().createTransaction().execute( { em ->
            Group group = em.createQuery('select g from sec$Group g', Group.class).getFirstResult();

            johnDoeUser = cont.metadata().create(User.class)
            johnDoeUser.login = 'johndoe'
            johnDoeUser.group = group
            em.persist(johnDoeUser)

            marySmithUser = cont.metadata().create(User.class)
            marySmithUser.login = 'marysmith'
            marySmithUser.group = group
            em.persist(marySmithUser)

            bobDylanUser = cont.metadata().create(User.class)
            bobDylanUser.login = 'bobdylan'
            bobDylanUser.group = group
            em.persist(bobDylanUser)

            def builder = ObjectGraphBuilderProvider.createBuilder(em)
            newProcessInstance = builder.procInstance(testId: 'procInstance1', procDefinition: procDefinition) {
                procActor(user: johnDoeUser, procRole: managerProcRole) {
                    procInstance(refId: 'procInstance1')
                }
                procActor(user: bobDylanUser, procRole: managerProcRole) {
                    procInstance(refId: 'procInstance1')
                }
                procActor(user: marySmithUser, procRole: operatorProcRole) {
                    procInstance(refId: 'procInstance1')
                }
            }
        } as Transaction.Runnable)


        newProcessInstance = cont.processRuntimeManager.startProcess(newProcessInstance, '', [:])

        ProcTask managerProcTask

        //check that procTask without procActor and with 2 candidate users created
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null',
                    ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            managerProcTask = procTasks[0]
            assertNull(managerProcTask.procActor)
            assertNull(managerProcTask.claimDate)
            assertNotNull(managerProcTask.startDate)
            assertEquals(2, managerProcTask.candidateUsers.size())
        } as Transaction.Runnable)

        cont.processRuntimeManager.claimProcTask(managerProcTask, bobDylanUser)

        //check that task is assigned to user who claimed it
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null',
                    ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            managerProcTask = procTasks[0]
            assertEquals(bobDylanUser, managerProcTask.procActor.user)
            assertNotNull(managerProcTask.claimDate)
        } as Transaction.Runnable)

    }

    @Test
    void testProcessLocalization() {
        ProcessMessagesManager processMessagesManager = AppBeans.get(ProcessMessagesManager.class)
        ProcDefinition procDefinition = cont.processRepositoryManager.deployProcessFromPath(BASIC_PROCESS_PATH, null, null)
        assertEquals ('Manager approval', processMessagesManager.getMessage(procDefinition.actId, "managerApproval"))
        assertEquals ('Approve', processMessagesManager.getMessage(procDefinition.actId, "managerApproval.approve"))
        assertEquals ('Утверждение менеджером', processMessagesManager.getMessage(procDefinition.actId, "managerApproval", new Locale("ru")))
        assertEquals ('Scanning', processMessagesManager.getMessage(procDefinition.actId, "scanning", new Locale("ru")))
        assertEquals ('Approve', processMessagesManager.findMessage(procDefinition.actId, "managerApproval.approve"))
        assertNull ( processMessagesManager.findMessage(procDefinition.actId, "nonExistingKey"))

    }

    @Test
    void testScriptTask() {
        ProcDefinition procDefinition = cont.processRepositoryManager.deployProcessFromPath(SCRIPT_TASK_PROCESS_PATH, null, null)
        ProcInstance procInstance
        cont.persistence().createTransaction().execute( {em ->
            procInstance = new ProcInstance(procDefinition: procDefinition)
            em.persist(procInstance)
        } as Transaction.Runnable)
        procInstance = cont.processRuntimeManager.startProcess(procInstance, '', [:])

        cont.persistence().createTransaction().execute( {em ->
            User user = em.createQuery("select u from sec\$User u where u.login = 'jack'").getFirstResult()
            assertNotNull(user)
        } as Transaction.Runnable)
    }

    @Test
    void testVariablesApi() {
        ProcDefinition procDefinition = cont.processRepositoryManager.deployProcessFromPath(VARIABLES_API_PROCESS_PATH, null, null)
        ProcInstance procInstance
        cont.persistence().createTransaction().execute( {em ->
            procInstance = new ProcInstance(procDefinition: procDefinition)
            em.persist(procInstance)
        } as Transaction.Runnable)
        procInstance = cont.processRuntimeManager.startProcess(procInstance, '', [:])

        def variablesManager = AppBeans.get(ProcessVariablesManager.class)
        variablesManager.setVariable(procInstance, 'a', 5)
        def execution = cont.runtimeService.createExecutionQuery().processInstanceId(procInstance.actProcessInstanceId).singleResult()
        cont.runtimeService.signal(execution.id)
        def variable = variablesManager.getVariable(procInstance, 'b')
        assertEquals(8, variable)
    }

    @Test
    void testMultipleEnterToTask() {
        ProcDefinition procDefinition = cont.processRepositoryManager.deployProcessFromPath(MULTIPLE_ENTER_TO_TASK_PROCESS_PATH, null, null)
        ProcInstance newProcessInstance

        User johnDoeUser

        ProcRole managerProcRole = procDefinition.procRoles.find {it.code == 'manager'}

        //create test users and proc actors
        cont.persistence().createTransaction().execute( { em ->
            Group group = em.createQuery('select g from sec$Group g', Group.class).getFirstResult();
            johnDoeUser = cont.metadata().create(User.class)
            johnDoeUser.login = 'johndoe'
            johnDoeUser.group = group
            em.persist(johnDoeUser)

            def builder = ObjectGraphBuilderProvider.createBuilder(em)
            newProcessInstance = builder.procInstance(testId: 'procInstance1', procDefinition: procDefinition) {
                procActor(user: johnDoeUser, procRole: managerProcRole, order: 0) {
                    procInstance(refId: 'procInstance1')
                }
            }
        } as Transaction.Runnable)

        newProcessInstance = cont.processRuntimeManager.startProcess(newProcessInstance, '', [:])

        ProcTask johnDoeProcTask

        //check that process task for manager created
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            johnDoeProcTask = procTasks.find { it.procActor.user.id == johnDoeUser.id }
            assertEquals('task1', johnDoeProcTask.actTaskDefinitionKey)
        } as Transaction.Runnable)

        cont.processRuntimeManager.completeProcTask(johnDoeProcTask, 'back', '', null)

        ProcTask johnDoeProcTask2

        //check that process task1 is created again
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            johnDoeProcTask2 = procTasks.find { it.procActor.user.id == johnDoeUser.id }
            assertEquals('task2', johnDoeProcTask2.actTaskDefinitionKey)
        } as Transaction.Runnable)

        cont.processRuntimeManager.completeProcTask(johnDoeProcTask2, 'do', '', null)

        ProcTask johnDoeProcTask1_2

        //check that process task1 is created again
        cont.persistence().createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            johnDoeProcTask1_2 = procTasks.find { it.procActor.user.id == johnDoeUser.id }
            assertEquals('task1', johnDoeProcTask1_2.actTaskDefinitionKey)
        } as Transaction.Runnable)

        cont.processRuntimeManager.completeProcTask(johnDoeProcTask1_2, 'forward', '', null)

        //check that process went on correct execution flow and finished
        cont.persistence().createTransaction().execute( { em ->
            newProcessInstance = em.reload(newProcessInstance)
            assertFalse(newProcessInstance.active)
        } as Transaction.Runnable)
    }
}
