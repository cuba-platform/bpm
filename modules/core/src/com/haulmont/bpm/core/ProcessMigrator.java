/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * Class is used for migrating procInstances to new procDefinition
 */
public interface ProcessMigrator {

    String NAME = "bpm_ProcessMigrator";

    void migrate(ProcessDefinition actProcessDefinition);

    void migrateProcTasks(ProcDefinition procDefinition, String actProcessDefinitionId);
}
