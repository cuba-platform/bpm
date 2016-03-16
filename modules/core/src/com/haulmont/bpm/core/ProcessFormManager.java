/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Class is used for access to process forms
 */
public interface ProcessFormManager {
    String NAME = "npm_ProcessFormManager";

    /**
     * Returns a map of process task outcomes. A key is an outcome name, a value is a form definition
     * for the outcome.
     * @param procTask process task
     * @return a map with task outcomes
     */
    Map<String, ProcFormDefinition> getOutcomesWithForms(ProcTask procTask);

    /**
     * Returns a form definition for process start event
     * @param procDefinition ProcDefinition instance
     * @return a form definition for the start event outcome
     */
    @Nullable
    ProcFormDefinition getStartForm(ProcDefinition procDefinition);

    /**
     * Returns a definition of the form that will be displayed when process is canceled by the user
     * @param procDefinition ProcDefinition instance
     * @return a form definition for cancel process action
     */
    ProcFormDefinition getCancelForm(ProcDefinition procDefinition);

    /**
     * Returns a definition of the form that will be used when outcomes aren't defined for the
     * process task
     * @param procDefinition ProcDefinition instance
     * @return a definition of default form
     */
    ProcFormDefinition getDefaultCompleteTaskForm(ProcDefinition procDefinition);
}
