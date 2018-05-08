/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.procinstance;

import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import org.apache.commons.lang.BooleanUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProcInstanceBrowse extends AbstractLookup {

    @Named("procInstancesTable.create")
    protected CreateAction createAction;

    @Named("procInstancesTable.edit")
    protected EditAction editAction;

    @Named("procInstancesTable.remove")
    protected RemoveAction removeAction;

    @Inject
    protected CollectionDatasource<ProcInstance, UUID> procInstancesDs;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected Table procInstancesTable;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;

    protected WindowConfig windowConfig;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        windowConfig = AppBeans.get(WindowConfig.class);

        createAction.setAfterCommitHandler(new CreateAction.AfterCommitHandler() {
            @Override
            public void handle(Entity entity) {
                ProcInstance reloadedEntity = (ProcInstance) dataManager.reload(entity, procInstancesDs.getView());
                procInstancesDs.updateItem(reloadedEntity);
            }
        });

        editAction.setAfterCommitHandler(new EditAction.AfterCommitHandler() {
            @Override
            public void handle(Entity entity) {
                ProcInstance reloadedEntity = (ProcInstance) dataManager.reload(entity, procInstancesDs.getView());
                procInstancesDs.updateItem(reloadedEntity);
            }
        });

        procInstancesDs.addItemChangeListener(e -> {
            boolean enabled = e.getItem() != null && BooleanUtils.isNotTrue(e.getItem().getActive());
            removeAction.setEnabled(enabled);
        });

        procInstancesTable.addGeneratedColumn("entityName", new Table.ColumnGenerator<ProcInstance>() {
            @Override
            public Component generateCell(final ProcInstance procInstance) {
                if (procInstance.getObjectEntityId() == null) return null;

                final MetaClass metaClass = metadata.getClass(procInstance.getEntityName());
                if (metaClass == null) {
                    Label label = componentsFactory.createComponent(Label.class);
                    label.setValue(procInstance.getEntityName());
                    return label;
                }

                LinkButton linkButton = componentsFactory.createComponent(LinkButton.class);
                linkButton.setCaption(metaClass.getName());
                linkButton.setAction(new AbstractAction("openScreen") {
                    @Override
                    public void actionPerform(Component component) {
                        LoadContext ctx = new LoadContext(metaClass).setQuery(
                                LoadContext.createQuery(String.format("select e from %s e where e.%s = :entityId",
                                        metaClass.getName(),
                                        referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass)))
                                        .setParameter("entityId", procInstance.getObjectEntityId()));
                        Entity entity = dataManager.load(ctx);
                        openEditor(windowConfig.getEditorScreenId(metaClass), entity, WindowManager.OpenType.THIS_TAB);
                    }
                });
                return linkButton;
            }
        });

        removeAction.setBeforeActionPerformedHandler(() -> {
            Set<ProcInstance> selected = procInstancesTable.getSelected();
            for (ProcInstance procInstance : selected) {
                if (Boolean.TRUE.equals(procInstance.getActive())) {
                    showNotification(getMessage("cannotRemoveActiveProcInstance"), NotificationType.ERROR);
                    return false;
                }
            }
            return true;
        });
    }
}