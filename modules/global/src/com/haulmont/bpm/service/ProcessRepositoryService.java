/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author gorbunkov
 */
public interface ProcessRepositoryService {

    String NAME = "bpm_ProcessRepositoryService";

    ProcDefinition deployProcessFromXML(String xml, @Nullable ProcDefinition procDefinition, @Nullable ProcModel procModel);

    String getProcessDefinitionXML(String actDeploymentId);

    String convertModelToProcessXml(String actModelId);

    void undeployProcess(String actDeploymentId);
}