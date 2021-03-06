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

package com.haulmont.bpm.gui.proctask;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
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

    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;

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
            Object entityId = procInstance.getObjectEntityId();

            MetaClass metaClass = metadata.getClass(procInstance.getEntityName());
            if (metaClass == null) {
                showNotification(formatMessage("metaClassNotFound", procInstance.getEntityName()), NotificationType.WARNING);
                return;
            }

            String editorScreenId = procInstance.getEntityEditorName();
            if (Strings.isNullOrEmpty(editorScreenId)) {
                editorScreenId = windowConfig.getEditorScreenId(metaClass);
            }

            LoadContext<Entity> ctx = new LoadContext<>(metaClass).setQuery(
                    LoadContext.createQuery(String.format("select e from %s e where e.%s = :entityId",
                            metaClass.getName(),
                            referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass)))
                            .setParameter("entityId", entityId));
            Entity entity = dataManager.load(ctx);
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
                    selectedTask.getProcInstance().getObjectEntityId() != null;
        }

        @Override
        public String getCaption() {
            return getMessage("openEntityEditor");
        }
    }
}