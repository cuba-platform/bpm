/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcFormDefinition;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

/**
 * @author gorbunkov
 * @version $Id$
 */
@ManagedBean(ProcessFormManager.NAME)
public class ProcessFromManagerBean implements ProcessFormManager {

    @Inject
    protected ExtensionElementsManager extensionElementsManager;

    @Inject
    protected TaskService taskService;

    @Override
    public Map<String, ProcFormDefinition> getOutcomesWithForms(ProcTask procTask) {
        Map<String, ProcFormDefinition> result = new HashMap<>();

        Task task = taskService.createTaskQuery().taskId(procTask.getActTaskId()).singleResult();
//        if (Strings.isNullOrEmpty(assignment.getName())
//                || assignment.getProcInstance() == null
//                || assignment.getProcInstance().getProcDefinition())
//
//        String actProcessDefinitionId = assignment.getProcInstance().getProcDefinition().getActId();
        Map<String, List<ExtensionElement>> extensionElements = extensionElementsManager.getTaskExtensionElements(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        List<ExtensionElement> outcomesElements = extensionElements.get("outcomes");
        if (outcomesElements != null) {
            ExtensionElement outcomesElement = outcomesElements.get(0);
            List<ExtensionElement> outcomeElementsList = outcomesElement.getChildElements().get("outcome");
            for (ExtensionElement outcomeElement : outcomeElementsList) {
                ProcFormDefinition formDefinition = null;
                List<ExtensionElement> formElements = outcomeElement.getChildElements().get("form");
                if (formElements != null && !formElements.isEmpty()) {
                    formDefinition = extractProcFormDefinition(formElements.get(0));
                }
                result.put(outcomeElement.getAttributeValue(null, "name"), formDefinition);
            }
        }
        return result;
    }

    @Override
    @Nullable
    public ProcFormDefinition getStartForm(ProcDefinition procDefinition) {
        Map<String, List<ExtensionElement>> extensionElements = extensionElementsManager.getStartExtensionElements(procDefinition.getActId());
        List<ExtensionElement> formElements = extensionElements.get("form");
        if (formElements != null && !formElements.isEmpty()) {
            return extractProcFormDefinition(formElements.get(0));
        }
        return null;
    }

    @Nullable
    protected ProcFormDefinition extractProcFormDefinition(ExtensionElement formElement) {
        Map<String, String> params = new HashMap<>();
        List<ExtensionElement> paramElements = formElement.getChildElements().get("param");
        if (paramElements != null) {
            for (ExtensionElement paramElement : paramElements) {
                params.put(paramElement.getAttributeValue(null, "name"), paramElement.getElementText());
            }
        }
        ProcFormDefinition procFormDefinition = new ProcFormDefinition();
        procFormDefinition.setName(formElement.getAttributeValue(null, "name"));
        procFormDefinition.setParams(params);
        return procFormDefinition;
    }
}
