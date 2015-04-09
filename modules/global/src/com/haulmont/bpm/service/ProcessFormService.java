/*
 * Copyright (c) 2015 com.haulmont.bpm.service
 */
package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.form.ProcFormDefinition;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author gorbunkov
 */
public interface ProcessFormService {
    String NAME = "bpm_ProcessFormService";

    Map<String, ProcFormDefinition> getOutcomesWithForms(ProcTask procTask);

    @Nullable
    ProcFormDefinition getStartForm(ProcDefinition procDefinition);
}