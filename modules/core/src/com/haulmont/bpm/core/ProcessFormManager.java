/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public interface ProcessFormManager {
    String NAME = "npm_ProcessFormManager";

    Map<String, ProcFormDefinition> getOutcomesWithForms(ProcTask procTask);

    @Nullable
    ProcFormDefinition getStartForm(ProcDefinition procDefinition);

    ProcFormDefinition getCancelForm(ProcDefinition procDefinition);

    ProcFormDefinition getDefaultCompleteTaskForm(ProcDefinition procDefinition);

    List<ProcFormDefinition> getAllForms();
}
