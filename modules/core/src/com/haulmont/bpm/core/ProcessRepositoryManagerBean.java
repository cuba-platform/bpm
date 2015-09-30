/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Resources;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;

import org.springframework.stereotype.Component;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
@Component(ProcessRepositoryManager.NAME)
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

    @Inject
    protected ModelTransformer modelTransformer;

    @Override
    public ProcDefinition deployProcessFromPath(String path, @Nullable ProcDefinition procDefinition, @Nullable ProcModel procModel) {
        String processXml = resources.getResourceAsString(path);
        if (processXml == null) {
            throw new BpmException("Error when deploying process. Resource "  + path + " not found");
        }
        return deployProcessFromXml(processXml, procDefinition, procModel);
    }

    @Override
    public ProcDefinition deployProcessFromXml(String xml, @Nullable ProcDefinition procDefinition, @Nullable ProcModel procModel) {
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
                procDefinition.setCode(activitiProcessDefinition.getId());
            } else {
                procDefinition = em.reload(procDefinition);
                if (procDefinition == null)
                    throw new BpmException("Error when deploying process. Process definition has been removed");
                processMigrator.migrate(activitiProcessDefinition);
                processMigrator.migrateProcTasks(procDefinition, activitiProcessDefinition.getId());
            }
            procDefinition.setName(activitiProcessDefinition.getName());
            procDefinition.setActId(activitiProcessDefinition.getId());
            procDefinition.setModel(procModel);
            procDefinition.setActive(true);
            em.persist(procDefinition);

            List<ProcRole> procRoles = syncProcRoles(procDefinition);
            procDefinition.setProcRoles(procRoles);

            tx.commit();
            return procDefinition;
        } finally {
            tx.end();
        }
    }

    protected List<ProcRole> syncProcRoles(ProcDefinition procDefinition) {
        List<ProcRole> result = new ArrayList<>();
        Map<String, List<ExtensionElement>> processExtensionElements = extensionElementsManager.getProcessExtensionElements(procDefinition.getActId());
        List<ExtensionElement> procRoles = processExtensionElements.get("procRoles");
        List<ProcRole> rolesToRemove = new ArrayList<>();
        if (procDefinition.getProcRoles() != null)
            rolesToRemove.addAll(procDefinition.getProcRoles());
        EntityManager em = persistence.getEntityManager();
        if (procRoles != null) {
            ExtensionElement procRolesElement = procRoles.get(0);
            List<ExtensionElement> procRoleElements = procRolesElement.getChildElements().get("procRole");
            int order = 0;
            for (ExtensionElement procRoleElement : procRoleElements) {
                String roleCode = procRoleElement.getAttributeValue(null, "code");
                String roleName = procRoleElement.getAttributeValue(null, "name");
                ProcRole existingProcRole = getProcRoleByCode(procDefinition, roleCode);
                if (existingProcRole == null) {
                    ProcRole procRole = metadata.create(ProcRole.class);
                    procRole.setCode(roleCode);
                    procRole.setName(roleName);
                    procRole.setOrder(order++);
                    procRole.setProcDefinition(procDefinition);
                    em.persist(procRole);
                    result.add(procRole);
                } else {
                    rolesToRemove.remove(existingProcRole);
                    existingProcRole.setOrder(order++);
                    existingProcRole.setName(roleName);
                    em.merge(existingProcRole);
                    result.add(existingProcRole);
                }
            }
        }
        for (ProcRole roleToRemove : rolesToRemove) {
            em.remove(roleToRemove);
        }
        return result;
    }

    @Nullable
    protected ProcRole getProcRoleByCode(ProcDefinition procDefinition, String code) {
        if (procDefinition.getProcRoles() != null) {
            for (ProcRole procRole : procDefinition.getProcRoles()) {
                if (code.equals(procRole.getCode())) return procRole;
            }
        }
        return null;
    }

    @Override
    public String getProcessDefinitionXml(String actProcessDefinitionId) {
        ProcessDefinition actProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(actProcessDefinitionId).singleResult();
        if (actProcessDefinition == null) return null;
        String actDeploymentId = actProcessDefinition.getDeploymentId();
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
    public String convertModelToProcessXml(String actModelId) {
        JsonNode editorNode;
        try {
            byte[] modelEditorSource = repositoryService.getModelEditorSource(actModelId);
            String modifiedModelJson = modelTransformer.transformModel(modelEditorSource);
            editorNode = new ObjectMapper().readTree(modifiedModelJson);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            BpmnModel model = jsonConverter.convertToBpmnModel(editorNode);
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            return new String(bpmnBytes, "utf-8");
        } catch (IOException e) {
            throw new BpmException("Error converting process model to XML", e);
        }
    }

    @Override
    public void undeployProcess(String actProcessDefinitionId) {
        Transaction tx = persistence.createTransaction();
        try {
            ProcessDefinition actProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(actProcessDefinitionId).singleResult();
            if (actProcessDefinition == null) return;
            String actDeploymentId = actProcessDefinition.getDeploymentId();
            repositoryService.deleteDeployment(actDeploymentId);
            tx.commit();
        } finally {
            tx.end();
        }
    }
}
