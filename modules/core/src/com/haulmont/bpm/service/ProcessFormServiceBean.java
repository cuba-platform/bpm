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