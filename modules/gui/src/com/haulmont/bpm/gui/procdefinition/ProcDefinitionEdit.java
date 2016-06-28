/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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
        tabSheet.addListener(new TabSheet.TabChangeListener() {
            @Override
            public void tabChanged(TabSheet.Tab newTab) {
                if ("xmlTab".equals(newTab.getName()) && !xmlTabInitialized) {
                    String processXml = processRepositoryService.getProcessDefinitionXml(getItem().getActId());
                    xmlField.setValue(processXml);
                    xmlTabInitialized = true;
                }
            }
        });
    }
}