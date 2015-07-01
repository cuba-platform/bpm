/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author gorbunkov
 * @version $Id$
 */
public interface ProcessRepositoryManager {

    String NAME = "bpm_ProcessRepositoryManager";

    ProcDefinition deployProcessFromPath(String path, @Nullable ProcDefinition procDefinition, @Nullable ProcModel model);

    ProcDefinition deployProcessFromXML(String xml, @Nullable ProcDefinition procDefinition, @Nullable ProcModel procModel);

    String getProcessDefinitionXML(String actProcessDefinitionId);

    String convertModelToProcessXML(String actModelId);

    void undeployProcess(String actProcessDefinitionId);
}
