/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Class provides a way to work process repository from a client tier. For documentation see {@code ProcessRepositoryManager}
 *
 */
public interface ProcessRepositoryService {

    String NAME = "bpm_ProcessRepositoryService";

    ProcDefinition deployProcessFromXml(String xml, @Nullable ProcDefinition procDefinition, @Nullable ProcModel procModel);

    String getProcessDefinitionXml(String actDeploymentId);

    String convertModelToProcessXml(String actModelId);

    void undeployProcess(String actDeploymentId);
}