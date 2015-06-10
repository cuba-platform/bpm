/*
 * Copyright (c) 2015 com.haulmont.bpm.gui.procdefinition
 */
package com.haulmont.bpm.gui.procdefinition;

import com.haulmont.bpm.entity.ProcRole;
import com.haulmont.bpm.service.ProcessRepositoryService;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.cuba.gui.components.SourceCodeEditor;
import com.haulmont.cuba.gui.components.TabSheet;
import com.haulmont.cuba.gui.data.CollectionDatasource;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**
 * @author gorbunkov
 */
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
                    String processXml = processRepositoryService.getProcessDefinitionXML(getItem().getActId());
                    xmlField.setValue(processXml);
                    xmlTabInitialized = true;
                }
            }
        });
    }
}