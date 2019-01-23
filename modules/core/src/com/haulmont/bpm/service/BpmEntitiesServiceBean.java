/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.bpm.service;

import com.google.common.base.Strings;
import com.haulmont.bpm.BpmConstants;
import com.haulmont.bpm.entity.*;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service(BpmEntitiesService.NAME)
public class BpmEntitiesServiceBean implements BpmEntitiesService {

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;

    @Override
    @Nullable
    public ProcDefinition findProcDefinitionByCode(String procDefinitionCode, String viewName) {
        ProcDefinition procDefinition;
        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();
            procDefinition = em.createQuery("select pd from bpm$ProcDefinition pd where pd.code = :code", ProcDefinition.class)
                    .setParameter("code", procDefinitionCode)
                    .setViewName(viewName)
                    .getFirstResult();
            tx.commit();
        }
        return procDefinition;
    }

    @Override
    public List<ProcInstance> findActiveProcInstancesForEntity(String procDefinitionCode, Entity entity, String viewName) {
        List<ProcInstance> procInstances;
        String referenceIdPropertyName = referenceToEntitySupport.getReferenceIdPropertyName(entity.getMetaClass());
        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();
            procInstances = em.createQuery("select pi from bpm$ProcInstance pi where " +
                    "pi.procDefinition.code = :procDefinitionCode and " +
                    "pi.active = true and " +
                    "pi.entity." + referenceIdPropertyName + " = :entityId", ProcInstance.class)
                    .setParameter("procDefinitionCode", procDefinitionCode)
                    .setParameter("entityId", referenceToEntitySupport.getReferenceId(entity))
                    .setViewName(viewName)
                    .getResultList();
            tx.commit();
        }
        return procInstances;
    }

    @Override
    public List<ProcTask> findActiveProcTasks(ProcInstance procInstance, User user, String viewName) {
        List<ProcTask> procTasks;
        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();
            procTasks = em.createQuery("select pt from bpm$ProcTask pt left join pt.procActor pa left join pa.user pau " +
                    "where pt.procInstance.id = :procInstanceId and (pau.id = :userId or " +
                    "(pa is null and exists(select pt2 from bpm$ProcTask pt2 join pt2.candidateUsers cu where pt2.id = pt.id and cu.id = :userId))) " +
                    "and pt.endDate is null", ProcTask.class)
                    .setParameter("procInstanceId", procInstance.getId())
                    .setParameter("userId", user.getId())
                    .setViewName(viewName)
                    .getResultList();
            tx.commit();
        }
        return procTasks;
    }

    @Override
    @Nullable
    public ProcRole findProcRole(String procDefinitionCode, String procRoleCode, String viewName) {
        ProcRole procRole;
        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();
            procRole = em.createQuery("select pr from bpm$ProcRole pr where " +
                    "pr.procDefinition.code = :procDefinitionCode and " +
                    "pr.code = :procRoleCode", ProcRole.class)
                    .setParameter("procDefinitionCode", procDefinitionCode)
                    .setParameter("procRoleCode", procRoleCode)
                    .setViewName(viewName)
                    .getFirstResult();
            tx.commit();
        }
        return procRole;
    }

    @Override
    public List<ProcTask> findActiveProcTasksForCurrentUser(ProcInstance procInstance, String viewName) {
        return findActiveProcTasks(procInstance, userSessionSource.getUserSession().getCurrentOrSubstitutedUser(), viewName);
    }

    @Override
    public ProcInstance createProcInstance(ProcInstanceDetails procInstanceDetails) {
        ProcDefinition procDefinition = procInstanceDetails.getProcDefinition();
        String procDefinitionCode = procInstanceDetails.getProcDefinitionCode();
        if (!Strings.isNullOrEmpty(procDefinitionCode)) {
            procDefinition = findProcDefinitionByCode(procDefinitionCode, BpmConstants.Views.PROC_DEFINITION_WITH_ROLES);
            if (procDefinition == null)
                throw new BpmException("ProcDefinition with code " + procDefinitionCode + " not found");
        }

        //reload procDefinition if required
        if (!PersistenceHelper.isNew(procDefinition) && !PersistenceHelper.isLoaded(procDefinition, "procRoles")) {
            for (ProcInstanceDetails.ProcActorDetails procActorDetails : procInstanceDetails.getProcActorDetails()) {
                if (!Strings.isNullOrEmpty(procActorDetails.getProcRoleCode())) {
                    procDefinition = dataManager.reload(procDefinition, BpmConstants.Views.PROC_DEFINITION_WITH_ROLES);
                }
            }
        }

        ProcInstance procInstance = metadata.create(ProcInstance.class);
        procInstance.setProcDefinition(procDefinition);
        Set<ProcActor> actors = new HashSet<>();
        for (ProcInstanceDetails.ProcActorDetails procActorDetails : procInstanceDetails.getProcActorDetails()) {
            ProcActor procActor = metadata.create(ProcActor.class);
            procActor.setProcInstance(procInstance);
            procActor.setUser(procActorDetails.getUser());
            if (procActorDetails.getProcRole() != null) {
                procActor.setProcRole(procActorDetails.getProcRole());
            } else {
                if (procDefinition.getProcRoles() != null && !procDefinition.getProcRoles().isEmpty()) {
                    ProcRole pr = procDefinition.getProcRoles().stream()
                            .filter(procRole -> procActorDetails.getProcRoleCode().equals(procRole.getCode()))
                            .findAny()
                            .orElse(null);
                    if (pr == null) {
                        throw new BpmException(String.format("ProcRole %s not found", procActorDetails.getProcRoleCode()));
                    }
                    procActor.setProcRole(pr);
                }
            }
            actors.add(procActor);
        }
        procInstance.setProcActors(actors);
        Entity entity = procInstanceDetails.getEntity();
        if (entity != null) {
            procInstance.setObjectEntityId(entity.getId());
            procInstance.setEntityName(entity.getMetaClass().getName());
        }
        return procInstance;
    }
}
