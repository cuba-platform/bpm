/*
 * Copyright (c) 2015 com.haulmont.bpm.service
 */
package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcDefinition;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author gorbunkov
 */
public interface ProcessRepositoryService {

    String NAME = "bpm_ProcessRepositoryService";

    ProcDefinition deployProcessFromXML(String xml, @Nullable ProcDefinition procDefinition);

    ProcDefinition deployProcessFromXML(String xml);

    String getProcessDefinitionXML(String actDeploymentId);

    String getProcessDefinitionXmlFromModel(String actModelId);

    List<ProcDefinition> getProcDefinitionsByProcessKey(String processKey);
}