/*
 * Copyright (c) 2015 com.haulmont.bpm.service
 */
package com.haulmont.bpm.service;

import com.haulmont.bpm.core.ProcessRepositoryManager;
import com.haulmont.bpm.entity.ProcDefinition;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * @author gorbunkov
 */
@Service(ProcessRepositoryService.NAME)
public class ProcessRepositoryServiceBean implements ProcessRepositoryService {

    @Inject
    protected ProcessRepositoryManager processRepositoryManager;

    @Override
    public ProcDefinition deployProcessFromXML(String xml, @Nullable ProcDefinition procDefinition) {
        return processRepositoryManager.deployProcessFromXML(xml, procDefinition);
    }

    @Override
    public ProcDefinition deployProcessFromXML(String xml) {
        return processRepositoryManager.deployProcessFromXML(xml);
    }

    @Override
    public String getProcessDefinitionXML(String actDeploymentId) {
        return processRepositoryManager.getProcessDefinitionXML(actDeploymentId);
    }
}