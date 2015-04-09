/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcRole;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Resources;
import org.activiti.bpmn.model.*;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

/**
 * @author gorbunkov
 * @version $Id$
 */
@ManagedBean(ProcessRepositoryManager.NAME)
public class ProcessRepositoryManagerBean implements ProcessRepositoryManager {

    @Inject
    protected RepositoryService repositoryService;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Resources resources;

    @Inject
    protected Persistence persistence;

    @Inject
    protected ProcessMigrator processMigrator;

    @Inject
    protected ExtensionElementsManager extensionElementsManager;

    @Override
    public ProcDefinition deployProcessFromPath(String path) {
        String processXml = resources.getResourceAsString(path);
        if (processXml == null) {
            throw new BpmException("Error when deploying process. Resource "  + path + " not found");
        }
        return deployProcessFromXML(processXml, null);
    }

    @Override
    public ProcDefinition deployProcessFromPath(String path, ProcDefinition procDefinition) {
        String processXml = resources.getResourceAsString(path);
        if (processXml == null) {
            throw new BpmException("Error when deploying process. Resource "  + path + " not found");
        }
        return deployProcessFromXML(processXml, procDefinition);
    }

    @Override
    public ProcDefinition deployProcessFromXML(String xml) {
        return deployProcessFromXML(xml, null);
    }

    @Override
    public ProcDefinition deployProcessFromXML(String xml, @Nullable ProcDefinition procDefinition) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Deployment deployment = repositoryService.createDeployment()
                    .addString("process.bpmn20.xml", xml)
                    .deploy();
            ProcessDefinition activitiProcessDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();
            if (procDefinition == null) {
                procDefinition = metadata.create(ProcDefinition.class);
            } else {
                procDefinition = em.reload(procDefinition);
                processMigrator.migrate(activitiProcessDefinition);
            }
            //noinspection ConstantConditions
            procDefinition.setName(activitiProcessDefinition.getName());
            procDefinition.setActKey(activitiProcessDefinition.getKey());
            procDefinition.setActId(activitiProcessDefinition.getId());
            procDefinition.setActVersion(activitiProcessDefinition.getVersion());
            procDefinition.setXml(xml);
            procDefinition.setActive(true);
            em.persist(procDefinition);

            List<ProcRole> procRoles = createProcRoles(procDefinition);
            procDefinition.setProcRoles(procRoles);

            tx.commit();
            return procDefinition;
        } finally {
            tx.end();
        }
    }

    protected List<ProcRole> createProcRoles(ProcDefinition procDefinition) {
        List<ProcRole> result = new ArrayList<>();
        Map<String, List<ExtensionElement>> processExtensionElements = extensionElementsManager.getProcessExtensionElements(procDefinition.getActId());
        List<ExtensionElement> procRoles = processExtensionElements.get("procRoles");
        if (procRoles != null) {
            List<String> existingProcRolesCodes = getExistingProcRolesCodes(procDefinition);
            EntityManager em = persistence.getEntityManager();
            ExtensionElement procRolesElement = procRoles.get(0);
            List<ExtensionElement> procRoleElements = procRolesElement.getChildElements().get("procRole");
            int order = 0;
            for (ExtensionElement procRoleElement : procRoleElements) {
                String roleCode = procRoleElement.getAttributeValue(null, "code");
                if (existingProcRolesCodes.contains(roleCode)) continue;
                ProcRole procRole = metadata.create(ProcRole.class);
                procRole.setName(procRoleElement.getAttributeValue(null, "name"));
                procRole.setCode(roleCode);
                procRole.setOrder(order++);
                procRole.setProcDefinition(procDefinition);
                em.persist(procRole);
                result.add(procRole);
            }
        }
        return result;
    }

    protected List<String> getExistingProcRolesCodes(ProcDefinition procDefinition) {
        List<String> result = new ArrayList<>();
        if (procDefinition.getProcRoles() != null) {
            for (ProcRole procRole : procDefinition.getProcRoles()) {
                result.add(procRole.getCode());
            }
        }
        return result;
    }
}
