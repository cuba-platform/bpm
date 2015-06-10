/*
 * Copyright (c) 2015 com.haulmont.bpm.service
 */
package com.haulmont.bpm.service;

import com.haulmont.bpm.core.ProcessFormManager;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.form.ProcFormDefinition;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 */
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
    public List<ProcFormDefinition> getAllForms() {
        return processFormManager.getAllForms();
    }

    @Override
    public ProcFormDefinition getDefaultCompleteTaskForm(ProcDefinition procDefinition) {
        return processFormManager.getDefaultCompleteTaskForm(procDefinition);
    }
}