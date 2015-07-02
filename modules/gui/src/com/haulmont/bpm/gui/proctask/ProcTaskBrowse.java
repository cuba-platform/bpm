/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.proctask;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.inject.Inject;
import java.util.*;

/**
 * @author gorbunkov
 */
public class ProcTaskBrowse extends AbstractLookup {

    @Inject
    protected CollectionDatasource<ProcTask, UUID> procTasksDs;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected ProcessMessagesService processMessagesService;

    @Inject
    protected Table procTasksTable;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        procTasksTable.addGeneratedColumn("assigned", new Table.ColumnGenerator<ProcTask>() {
            @Override
            public Component generateCell(ProcTask entity) {
                CheckBox checkBox = componentsFactory.createComponent(CheckBox.class);
                checkBox.setValue(entity.getProcActor() != null);
                return checkBox;
            }
        });

        procTasksTable.setItemClickAction(procTasksTable.getAction("openProcInstance"));
    }


    public void openProcInstance() {
        Window window = openEditor("bpm$ProcInstance.edit", procTasksDs.getItem().getProcInstance(), WindowManager.OpenType.THIS_TAB);
        window.addListener(new CloseListener() {
            @Override
            public void windowClosed(String actionId) {
                if (COMMIT_ACTION_ID.equals(actionId)) {
                    procTasksDs.refresh();
                }
            }
        });
    }
}