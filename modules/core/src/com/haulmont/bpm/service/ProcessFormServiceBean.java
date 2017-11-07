/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.core.ProcessFormManager;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Map;

@Service(ProcessFormService.NAME)
public class ProcessFormServiceBean implements ProcessFormService {

    @Inject
    protected ProcessFormManager processFormManager;

    @Override
    public Map<String, ProcFormDefinition> getOutcomesWithForms(ProcTask procTask) {
        return processFormManager.getOutcomesWithForms(procTask);
    }

    @Override
    @Nullable
    public ProcFormDefinition getStartForm(ProcDefinition procDefinition) {
        return processFormManager.getStartForm(procDefinition);
    }

    @Override
    public ProcFormDefinition getCancelForm(ProcDefinition procDefinition) {
        return processFormManager.getCancelForm(procDefinition);
    }

    @Override
    public ProcFormDefinition getDefaultCompleteTaskForm(ProcDefinition procDefinition) {
        return processFormManager.getDefaultCompleteTaskForm(procDefinition);
    }
}