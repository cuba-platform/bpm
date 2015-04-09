/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;

import javax.annotation.Nullable;
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
}
