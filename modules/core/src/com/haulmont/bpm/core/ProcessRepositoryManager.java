/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;

import java.util.List;

/**
 * @author gorbunkov
 * @version $Id$
 */
public interface ProcessRepositoryManager {

    String NAME = "bpm_ProcessRepositoryManager";

    ProcDefinition deployProcessFromPath(String path);

    ProcDefinition deployProcessFromPath(String path, ProcDefinition procDefinition);

    ProcDefinition deployProcessFromXML(String xml);

    ProcDefinition deployProcessFromXML(String xml, ProcDefinition procDefinition);

    String getProcessDefinitionXML(String actDeploymentId);

    String getProcessDefinitionXmlFromModel(String actModelId);

    List<ProcDefinition> getProcDefinitionsByProcessKey(String processKey);
}
