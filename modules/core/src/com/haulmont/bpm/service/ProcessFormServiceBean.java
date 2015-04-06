/*
 * Copyright (c) 2015 com.haulmont.bpm.service
 */
package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcFormDefinition;
import com.haulmont.bpm.core.ProcessFormManager;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcDefinition;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
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
}