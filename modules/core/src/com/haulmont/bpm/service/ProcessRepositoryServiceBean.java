/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.core.ProcessRepositoryManager;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

/**
 * @author gorbunkov
 */
@Service(ProcessRepositoryService.NAME)
public class ProcessRepositoryServiceBean implements ProcessRepositoryService {

    @Inject
    protected ProcessRepositoryManager processRepositoryManager;

    @Override
    public ProcDefinition deployProcessFromXML(String xml, @Nullable ProcDefinition procDefinition, @Nullable ProcModel procModel) {
        return processRepositoryManager.deployProcessFromXML(xml, procDefinition, procModel);
    }

    @Override
    public String getProcessDefinitionXML(String actDeploymentId) {
        return processRepositoryManager.getProcessDefinitionXML(actDeploymentId);
    }

    @Override
    public String convertModelToProcessXml(String actModelId) {
        return processRepositoryManager.convertModelToProcessXML(actModelId);
    }

    @Override
    public void undeployProcess(String actDeploymentId) {
        processRepositoryManager.undeployProcess(actDeploymentId);
    }
}