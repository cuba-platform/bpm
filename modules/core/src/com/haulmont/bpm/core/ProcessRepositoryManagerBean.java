/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;
import com.haulmont.bpm.entity.ProcRole;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Resources;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
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
                if (procDefinition == null)
                    throw new BpmException("Error when deploying process. Process definition has been removed");
                processMigrator.migrate(activitiProcessDefinition);
                processMigrator.migrateProcTasks(procDefinition, activitiProcessDefinition.getId());
            }
            procDefinition.setName(activitiProcessDefinition.getName());
            procDefinition.setActKey(activitiProcessDefinition.getKey());
            procDefinition.setActId(activitiProcessDefinition.getId());
            procDefinition.setActDeploymentId(activitiProcessDefinition.getDeploymentId());
            procDefinition.setActVersion(activitiProcessDefinition.getVersion());
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

    @Override
    public String getProcessDefinitionXML(String actDeploymentId) {
        List<String> deploymentResourceNames = repositoryService.getDeploymentResourceNames(actDeploymentId);
        String deploymentResourceName = null;
        for (String name : deploymentResourceNames) {
            if (name.endsWith("bpmn20.xml")) {
                deploymentResourceName = name;
                break;
            }
        }
        if (Strings.isNullOrEmpty(deploymentResourceName))
            throw new BpmException("Cannot find process xml resource");
        InputStream is = repositoryService.getResourceAsStream(actDeploymentId, deploymentResourceName);
        try {
            return IOUtils.toString(is);
        } catch (IOException e) {
            throw new BpmException("Error reading process xml", e);
        }
    }

    @Override
    public String convertModelToProcessXML(String actModelId) {
        JsonNode editorNode;
        try {
            editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(actModelId));
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            BpmnModel model = jsonConverter.convertToBpmnModel(editorNode);
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            return new String(bpmnBytes, "utf-8");
        } catch (IOException e) {
            throw new BpmException("Error converting process model to XML", e);
        }
    }

    @Override
    public List<ProcDefinition> getProcDefinitionsByProcessKey(String processKey) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<ProcDefinition> query = em.createQuery("select pd from bpm$ProcDefinition pd where pd.actKey = :actKey", ProcDefinition.class);
            query.setParameter("actKey", processKey);
            List<ProcDefinition> result = query.getResultList();
            tx.commit();
            return result;
        } finally {
            tx.end();
        }
    }

    @Override
    public void undeployProcess(String actDeploymentId) {
        Transaction tx = persistence.createTransaction();
        try {
            repositoryService.deleteDeployment(actDeploymentId);
            tx.commit();
        } finally {
            tx.end();
        }
    }
}
