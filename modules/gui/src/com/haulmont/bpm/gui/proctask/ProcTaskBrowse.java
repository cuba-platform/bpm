/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.proctask;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

public class ProcTaskBrowse extends AbstractLookup {

    @Inject
    protected CollectionDatasource<ProcTask, UUID> procTasksDs;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected ProcessMessagesService processMessagesService;

    @Inject
    protected Table<ProcTask> procTasksTable;

    @Inject
    protected Metadata metadata;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected Button openEntityEditorBtn;

    protected WindowConfig windowConfig;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        windowConfig = AppBeans.get(WindowConfig.class);

        procTasksTable.addGeneratedColumn("assigned", entity -> {
            CheckBox checkBox = componentsFactory.createComponent(CheckBox.class);
            checkBox.setValue(entity.getProcActor() != null);
            return checkBox;
        });

        Action openEntityEditorAction = new OpenEntityEditorAction();
        procTasksTable.addAction(openEntityEditorAction);
        openEntityEditorBtn.setAction(openEntityEditorAction);

        procTasksTable.setItemClickAction(openEntityEditorAction);
    }

    public void openProcInstance() {
        Window window = openEditor("bpm$ProcInstance.edit", procTasksDs.getItem().getProcInstance(), WindowManager.OpenType.THIS_TAB);
        window.addCloseListener(actionId -> {
            if (COMMIT_ACTION_ID.equals(actionId)) {
                procTasksDs.refresh();
            }
        });
    }

    protected class OpenEntityEditorAction extends ItemTrackingAction {

        public OpenEntityEditorAction() {
            super("openEntityEditor");
        }

        @Override
        public void actionPerform(Component component) {
            ProcTask selectedTask = procTasksTable.getSingleSelected();
            ProcInstance procInstance = selectedTask.getProcInstance();
            UUID entityId = procInstance.getEntityId();

            MetaClass metaClass = metadata.getClass(procInstance.getEntityName());
            if (metaClass == null) {
                showNotification(formatMessage("metaClassNotFound", procInstance.getEntityName()), NotificationType.WARNING);
                return;
            }

            String editorScreenId = procInstance.getEntityEditorName();
            if (Strings.isNullOrEmpty(editorScreenId)) {
                editorScreenId = windowConfig.getEditorScreenId(metaClass);
            }

            Entity entity = dataManager.load(new LoadContext<>(metaClass).setId(entityId));
            if (entity == null) {
                showNotification(formatMessage("entityNotFound", entityId), NotificationType.WARNING);
                return;
            }

            Window window = openEditor(editorScreenId, entity, WindowManager.OpenType.THIS_TAB);
            window.addCloseListener(actionId -> {
                if (COMMIT_ACTION_ID.equals(actionId)) {
                    procTasksDs.refresh();
                }
            });
        }

        @Override
        protected boolean isApplicable() {
            ProcTask selectedTask = procTasksTable.getSingleSelected();
            return selectedTask != null &&
                    selectedTask.getProcInstance() != null &&
                    selectedTask.getProcInstance().getEntityId() != null;
        }

        @Override
        public String getCaption() {
            return getMessage("openEntityEditor");
        }
    }
}