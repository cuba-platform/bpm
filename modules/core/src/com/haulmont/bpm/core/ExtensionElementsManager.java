/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import org.activiti.bpmn.model.ExtensionElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Helper class for working with extension elements of BPMN model elements
 *
 * @author gorbunkov
 * @version $Id$
 */
public interface ExtensionElementsManager {

    String NAME = "bpm_ExtensionElementsManager";

    Map<String, List<ExtensionElement>> getFlowElementExtensionElements(String actProcessDefinitionId, String actFlowElementDefinitionKey);

    Map<String, List<ExtensionElement>> getStartExtensionElements(String actProcessDefinitionId);

    Map<String, List<ExtensionElement>> getProcessExtensionElements(String actProcessDefinitionId);

    @Nullable
    String getTimerOutcome(String actProcessDefinitionId, String actBoundaryEventDefinitionKey);

    @Nullable
    String getTaskProcRole(String actProcessDefinitionId, String actTaskDefinitionKey);
}
