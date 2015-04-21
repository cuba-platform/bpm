/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.exception.BpmException;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.RepositoryService;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
@ManagedBean(ExtensionElementsManager.NAME)
public class ExtensionElementsManagerBean implements ExtensionElementsManager {

    @Inject
    protected RepositoryService repositoryService;

    @Override
    public Map<String, List<ExtensionElement>> getFlowElementExtensionElements(String actProcessDefinitionId, String actFlowElementDefinitionKey) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(actProcessDefinitionId);
        FlowElement flowElement = bpmnModel.getFlowElement(actFlowElementDefinitionKey);
        return flowElement.getExtensionElements();
    }

    @Override
    public Map<String, List<ExtensionElement>> getStartExtensionElements(String actProcessDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(actProcessDefinitionId);
        List<StartEvent> startEvents = bpmnModel.getMainProcess().findFlowElementsOfType(StartEvent.class);
        if (startEvents.size() != 1) {
            throw new BpmException("Cannot process process with multiple start events");
        }
        StartEvent startEvent = startEvents.get(0);
        return startEvent.getExtensionElements();
    }

    @Override
    public Map<String, List<ExtensionElement>> getProcessExtensionElements(String actProcessDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(actProcessDefinitionId);
        Process mainProcess = bpmnModel.getMainProcess();
        return mainProcess.getExtensionElements();
    }

    @Override
    public String getTimerOutcome(String actProcessDefinitionId, String actBoundaryEventDefinitionKey) {
        Map<String, List<ExtensionElement>> flowElementExtensionElements = getFlowElementExtensionElements(actProcessDefinitionId, actBoundaryEventDefinitionKey);
        List<ExtensionElement> outcomeElements = flowElementExtensionElements.get("outcome");
        if (outcomeElements != null) {
            ExtensionElement outcomeElement = outcomeElements.get(0);
            return outcomeElement.getElementText();
        }
        return null;
    }
}
