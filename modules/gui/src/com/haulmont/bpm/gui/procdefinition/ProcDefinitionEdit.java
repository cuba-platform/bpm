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

package com.haulmont.bpm.gui.procdefinition;

import com.haulmont.bpm.service.ProcessRepositoryService;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.cuba.gui.components.SourceCodeEditor;
import com.haulmont.cuba.gui.components.TabSheet;

import javax.inject.Inject;
import java.util.Map;

public class ProcDefinitionEdit extends AbstractEditor<ProcDefinition> {

    @Inject
    protected TabSheet tabSheet;

    @Inject
    protected ProcessRepositoryService processRepositoryService;

    @Inject
    protected SourceCodeEditor xmlField;

    protected boolean xmlTabInitialized;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initTabSheet();
    }

    protected void initTabSheet() {
        tabSheet.addSelectedTabChangeListener(event -> {
            if ("xmlTab".equals(event.getSelectedTab().getName()) && !xmlTabInitialized) {
                String processXml = processRepositoryService.getProcessDefinitionXml(getItem().getActId());
                xmlField.setValue(processXml);
                xmlTabInitialized = true;
            }
        });
    }
}