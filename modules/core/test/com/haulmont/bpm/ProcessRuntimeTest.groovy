/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm

import com.haulmont.bpm.core.ProcessMessagesManager
import com.haulmont.bpm.entity.ProcDefinition
import com.haulmont.bpm.entity.ProcInstance
import com.haulmont.bpm.entity.ProcRole
import com.haulmont.bpm.entity.ProcTask
import com.haulmont.bpm.testsupport.BpmTestCase
import com.haulmont.bpm.testsupport.ObjectGraphBuilderProvider
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.security.entity.User

/**
 *
 * @author gorbunkov
 * @version $Id$
 */
class ProcessRuntimeTest extends BpmTestCase {

    static final String BASIC_PROCESS_PATH = "com/haulmont/bpm/process/testBasic.bpmn20.xml";
    static final String MULTI_INSTANCE_PARALLEL_PROCESS_PATH = "com/haulmont/bpm/process/testMultiInstanceParallel.bpmn20.xml";
    static final String MULTI_INSTANCE_SEQUENTIAL_PROCESS_PATH = "com/haulmont/bpm/process/testMultiInstanceSequential.bpmn20.xml";
    static final String CLAIM_TASK_PROCESS_PATH = "com/haulmont/bpm/process/testClaimTask.bpmn20.xml";
    static final String SCRIPT_TASK_PROCESS_PATH = "com/haulmont/bpm/process/testScriptTask.bpmn20.xml";


    void testBasic() {
        ProcDefinition procDefinition = processRepositoryManager.deployProcessFromPath(BASIC_PROCESS_PATH)

        //check that procInstance object is created in bpm database and internal activiti database
        persistence.createTransaction().execute( {em ->
            def reloadedProcDefinition = em.createQuery('select pd from bpm$ProcDefinition pd where pd.id = :id')
                    .setParameter('id', procDefinition.id)
                    .getFirstResult()
            assertNotNull(reloadedProcDefinition)

            def activitiProcessDefinitions = repositoryService.createProcessDefinitionQuery()
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
        persistence.createTransaction().execute( { em ->
            johnDoeUser = metadata.create(User.class)
            johnDoeUser.login = 'johndoe'
            em.persist(johnDoeUser)

            marySmithUser = metadata.create(User.class)
            marySmithUser.login = 'marysmith'
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

        newProcessInstance = processRuntimeManager.startProcess(newProcessInstance, '', [:])

        //check that proctask was created for user with manager role
        ProcTask procTask
        persistence.createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            procTask = procTasks[0]
            assertNotNull(procTask)
            assertEquals(johnDoeUser.id, procTask.procActor.user.id)
        } as Transaction.Runnable)

        //test process forms
        Map procTaskForms = processFormManager.getOutcomesWithForms(procTask)
        assertEquals(2, procTaskForms.size())

        def approveForm = procTaskForms['approve']
        assertEquals('standardProcessForm', approveForm.name)
        assertEquals(2, approveForm.params.size())
        assertEquals("true", approveForm.params['commentRequired'])
        assertEquals("true", approveForm.params['attachmentsVisible'])

        def rejectForm = procTaskForms['reject']
        assertEquals('someOtherProcessForm', rejectForm.name)
        assertEquals(0, rejectForm.params.size())

        def startForm = processFormManager.getStartForm(procDefinition)
        assertEquals("startProcessForm", startForm.name)
        assertEquals(1, startForm.params.size())
        assertEquals("true", startForm.params['procActorsVisible'])

        processRuntimeManager.completeProcTask(procTask, 'approve', 'Scanning approved by manager', [:])

        persistence.createTransaction().execute ({ em ->
            procTask = em.reload(procTask)
            assertNotNull(procTask)
            assertNotNull(procTask.endDate)
            assertEquals('approve', procTask.outcome)
            assertEquals('Scanning approved by manager', procTask.comment)
        } as Transaction.Runnable)

        //check that process went on a correct flow of execution
        persistence.createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            procTask = procTasks[0]
            assertNotNull(procTask)
            assertEquals('scanning', procTask.name)
            assertEquals(marySmithUser.id, procTask.procActor.user.id)
        } as Transaction.Runnable)

        processRuntimeManager.completeProcTask(procTask, 'complete', 'Scanning completed', [:])

        //check that process completed
        persistence.createTransaction().execute( { em ->
            newProcessInstance = em.reload(newProcessInstance)
            assertFalse(newProcessInstance.active)
        } as Transaction.Runnable)
    }

    void testMultiInstanceParallel() {
        ProcDefinition procDefinition = processRepositoryManager.deployProcessFromPath(MULTI_INSTANCE_PARALLEL_PROCESS_PATH)
        ProcInstance newProcessInstance

        User johnDoeUser
        User marySmithUser
        User bobDylanUser

        ProcRole managerProcRole = procDefinition.procRoles.find {it.code == 'manager'}
        ProcRole operatorProcRole = procDefinition.procRoles.find {it.code == 'operator'}

        //create test users and proc actors
        persistence.createTransaction().execute( { em ->
            johnDoeUser = metadata.create(User.class)
            johnDoeUser.login = 'johndoe'
            em.persist(johnDoeUser)

            marySmithUser = metadata.create(User.class)
            marySmithUser.login = 'marysmith'
            em.persist(marySmithUser)

            bobDylanUser = metadata.create(User.class)
            bobDylanUser.login = 'bobdylan'
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

        newProcessInstance = processRuntimeManager.startProcess(newProcessInstance, '', [:])


        ProcTask johnDoeProcTask
        ProcTask bobDylanProcTask

        //check that 2 process tasks created
        persistence.createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(2, procTasks.size())

            johnDoeProcTask = procTasks.find { it.procActor.user.id == johnDoeUser.id }
            bobDylanProcTask = procTasks.find { it.procActor.user.id == bobDylanUser.id }

            assertEquals('managerApproval', johnDoeProcTask.name)
            assertEquals('managerApproval', bobDylanProcTask.name)
        } as Transaction.Runnable)

        processRuntimeManager.completeProcTask(johnDoeProcTask, 'approve', '')

        //check that one process task is still active
        persistence.createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            bobDylanProcTask = procTasks.find { it.procActor.user.id == bobDylanUser.id }
            assertEquals('managerApproval', bobDylanProcTask.name)
        } as Transaction.Runnable)

        processRuntimeManager.completeProcTask(bobDylanProcTask, 'reject', '')

        //check that process went on correct execution flow and finished
        persistence.createTransaction().execute( { em ->
            newProcessInstance = em.reload(newProcessInstance)
            assertFalse(newProcessInstance.active)
        } as Transaction.Runnable)
    }

    void testMultiInstanceSequential() {
        ProcDefinition procDefinition = processRepositoryManager.deployProcessFromPath(MULTI_INSTANCE_SEQUENTIAL_PROCESS_PATH)
        ProcInstance newProcessInstance

        User johnDoeUser
        User marySmithUser
        User bobDylanUser

        ProcRole managerProcRole = procDefinition.procRoles.find {it.code == 'manager'}
        ProcRole operatorProcRole = procDefinition.procRoles.find {it.code == 'operator'}

        //create test users and proc actors
        persistence.createTransaction().execute( { em ->
            johnDoeUser = metadata.create(User.class)
            johnDoeUser.login = 'johndoe'
            em.persist(johnDoeUser)

            marySmithUser = metadata.create(User.class)
            marySmithUser.login = 'marysmith'
            em.persist(marySmithUser)

            bobDylanUser = metadata.create(User.class)
            bobDylanUser.login = 'bobdylan'
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

        newProcessInstance = processRuntimeManager.startProcess(newProcessInstance, '', [:])

        ProcTask johnDoeProcTask
        ProcTask bobDylanProcTask

        //check that process task for first manager created
        persistence.createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            johnDoeProcTask = procTasks.find { it.procActor.user.id == johnDoeUser.id }
            assertEquals('managerApproval', johnDoeProcTask.name)
        } as Transaction.Runnable)

        processRuntimeManager.completeProcTask(johnDoeProcTask, 'approve', '')

        //check that process task for second manager created
        persistence.createTransaction().execute( { em ->
            def query = em.createQuery('select a from bpm$ProcTask a where a.procInstance.id = :procInstance and a.endDate is null', ProcTask.class)
            query.setParameter('procInstance', newProcessInstance)
            def procTasks = query.getResultList()
            assertEquals(1, procTasks.size())
            bobDylanProcTask = procTasks.find { it.procActor.user.id == bobDylanUser.id }
            assertEquals('managerApproval', bobDylanProcTask.name)
        } as Transaction.Runnable)

        processRuntimeManager.completeProcTask(bobDylanProcTask, 'reject', '')

        //check that process went on correct execution flow and finished
        persistence.createTransaction().execute( { em ->
            newProcessInstance = em.reload(newProcessInstance)
            assertFalse(newProcessInstance.active)
        } as Transaction.Runnable)
    }

    void testClaimTask() {
        ProcDefinition procDefinition = processRepositoryManager.deployProcessFromPath(CLAIM_TASK_PROCESS_PATH)

        ProcInstance newProcessInstance
        User johnDoeUser
        User marySmithUser
        User bobDylanUser

        def managerProcRole = procDefinition.procRoles.find {it.code == 'manager'}
        def operatorProcRole = procDefinition.procRoles.find {it.code == 'operator'}

        persistence.createTransaction().execute( { em ->
            johnDoeUser = metadata.create(User.class)
            johnDoeUser.login = 'johndoe'
            em.persist(johnDoeUser)

            marySmithUser = metadata.create(User.class)
            marySmithUser.login = 'marysmith'
            em.persist(marySmithUser)

            bobDylanUser = metadata.create(User.class)
            bobDylanUser.login = 'bobdylan'
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


        newProcessInstance = processRuntimeManager.startProcess(newProcessInstance, '', [:])

        ProcTask managerProcTask

        //check that procTask without procActor and with 2 candidate users created
        persistence.createTransaction().execute( { em ->
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

        processRuntimeManager.claimProcTask(managerProcTask, bobDylanUser)

        //check that task is assigned to user who claimed it
        persistence.createTransaction().execute( { em ->
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

    void testProcessLocalization() {
        ProcessMessagesManager processMessagesManager = AppBeans.get(ProcessMessagesManager.class)
        ProcDefinition procDefinition = processRepositoryManager.deployProcessFromPath(BASIC_PROCESS_PATH)
        assertEquals ('Manager approval', processMessagesManager.getMessage(procDefinition, "managerApproval"))
        assertEquals ('Approve', processMessagesManager.getMessage(procDefinition, "managerApproval.approve"))
        assertEquals ('Утверждение менеджером', processMessagesManager.getMessage(procDefinition, "managerApproval", new Locale("ru")))
        assertEquals ('Scanning', processMessagesManager.getMessage(procDefinition, "scanning", new Locale("ru")))
    }

    void testScriptTask() {
        ProcDefinition procDefinition = processRepositoryManager.deployProcessFromPath(SCRIPT_TASK_PROCESS_PATH)
        ProcInstance procInstance
        persistence.createTransaction().execute( {em ->
            procInstance = new ProcInstance(procDefinition: procDefinition)
            em.persist(procInstance)
        } as Transaction.Runnable)
        procInstance = processRuntimeManager.startProcess(procInstance, '', [:])

        persistence.createTransaction().execute( {em ->
            User user = em.createQuery("select u from sec\$User u where u.login = 'jack'").getFirstResult()
            assertNotNull(user)
        } as Transaction.Runnable)

    }
}
