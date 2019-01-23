/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    String NAME = "bpm_ProcessFormManager";

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
