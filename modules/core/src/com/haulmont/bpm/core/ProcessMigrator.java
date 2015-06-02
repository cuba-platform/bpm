/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author gorbunkov
 * @version $Id$
 */
public interface ProcessMigrator {

    String NAME = "bpm_ProcessMigrator";

    void migrate(ProcessDefinition actProcessDefinition);

    void migrateProcTasks(ProcDefinition procDefinition, String actProcessDefinitionId);
}
