/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.procinstance;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.ProcActor;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcRole;
import com.haulmont.bpm.gui.action.ProcAction;
import com.haulmont.bpm.gui.procactions.ProcActionsFrame;
import com.haulmont.bpm.gui.procactor.ProcActorsFrame;
import com.haulmont.bpm.gui.procattachment.ProcAttachmentsFrame;
import com.haulmont.bpm.gui.proctask.ProcTasksFrame;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.HasUuid;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.global.UserSession;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class ProcInstanceEdit extends AbstractEditor<ProcInstance> {

    @Inject
    protected FieldGroup fieldGroup;

    @Inject
    protected Datasource<ProcInstance> procInstanceDs;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ProcActorsFrame procActorsFrame;

    @Named("procActorsFrame.procActorsDs")
    protected CollectionDatasource<ProcActor, UUID> procActorsDs;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected ProcAttachmentsFrame procAttachmentsFrame;

    @Named("fieldGroup.procDefinition")
    protected LookupField procDefinitionLookup;

    protected LookupField entityNameLookup;

    protected PickerField entityIdPickerField;

    @Named("fieldGroup.entityEditorName")
    protected TextField entityEditorNameField;

    @Inject
    protected ProcActionsFrame procActionsFrame;

    @Inject
    protected ProcTasksFrame procTasksFrame;

    @Inject
    protected UserSession userSession;

    protected boolean entityDetailsVisible = false;
    protected LinkButton openEntityBtn;

    @Override
    public void setItem(Entity item) {
        super.setItem(item);

        addFieldGroupCustomFields();
        setComponentsVisible();
        setComponentsEditable();

        procInstanceDs.addItemPropertyChangeListener(e -> {
            switch (e.getProperty()) {
                case "procDefinition":
                    procActorsFrame.setProcInstance(getItem());
                    procActorsFrame.refresh();
                    initProcActors((ProcDefinition) e.getValue());
                    break;
                case "entityEditorName":
                    PickerField.LookupAction action = (PickerField.LookupAction) entityIdPickerField.getAction(PickerField.LookupAction.NAME);
                    if (action != null) {
                        action.setLookupScreen((String) e.getValue());
                    }
                    break;
            }
        });

        procActorsFrame.setProcInstance(getItem());
        procActorsFrame.refresh();

        procAttachmentsFrame.setProcInstance(getItem());
        procAttachmentsFrame.refresh();

        procTasksFrame.setProcInstance(getItem());
        procTasksFrame.refresh();
    }


    @Override
    protected void postInit() {
        super.postInit();
        initProcTaskActionsFrame();
    }

    protected void setComponentsEditable() {
        if (getItem().getStartDate() != null) {
            procDefinitionLookup.setEditable(false);
        }
        if (getItem().getStartedBy() != null
                && !userSession.getCurrentOrSubstitutedUser().equals(getItem().getStartedBy())) {
            entityNameLookup.setEditable(false);
            entityIdPickerField.setEditable(false);
            entityEditorNameField.setEditable(false);
        }
    }

    protected void setComponentsVisible() {
        if (getItem().getCancelled()) {
            fieldGroup.setVisible("cancelled", true);
            fieldGroup.setVisible("cancelComment", true);
        }
    }

    protected void initProcTaskActionsFrame() {
        procActionsFrame.initializer()
                .setBeforeStartProcessPredicate(() -> {
                    if (PersistenceHelper.isNew(getItem())) {
                        showNotification(getMessage("saveProcInstance"), NotificationType.WARNING);
                        return false;
                    } else {
                        return commit();
                    }
                })
                .setAfterStartProcessListener(new MessageAndCloseAfterActionListener(getMessage("processStarted")))
                .setAfterCancelProcessListener(new MessageAndCloseAfterActionListener(getMessage("processCancelled")))
                .setAfterCompleteTaskListener(new MessageAndCloseAfterActionListener(getMessage("taskCompleted")))
                .setAfterClaimTaskListener(new MessageAndCloseAfterActionListener(getMessage("taskClaimed")))
                .setBeforeCompleteTaskPredicate(new CommitEditorBeforeActionPredicate())
                .setBeforeClaimTaskPredicate(new CommitEditorBeforeActionPredicate())
                .setBeforeCancelProcessPredicate(new CommitEditorBeforeActionPredicate())
                .init(getItem());
    }

    protected void addFieldGroupCustomFields() {
        fieldGroup.addCustomField("entityLink", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                HBoxLayout hbox = componentsFactory.createComponent(HBoxLayout.class);
                openEntityBtn = componentsFactory.createComponent(LinkButton.class);
                initOpenEntityBtn();
                final LinkButton detailsBtn = componentsFactory.createComponent(LinkButton.class);
                detailsBtn.setCaption(getMessage("showDetails"));
                detailsBtn.setAction(new BaseAction("") {
                    @Override
                    public void actionPerform(Component component) {
                        entityDetailsVisible = !entityDetailsVisible;
                        fieldGroup.setVisible("entityName", entityDetailsVisible);
                        fieldGroup.setVisible("entityId", entityDetailsVisible);
                        fieldGroup.setVisible("entityEditorName", entityDetailsVisible);

                        detailsBtn.setCaption(getMessage(entityDetailsVisible ? "hideDetails" : "showDetails"));
                    }
                });

                hbox.add(openEntityBtn);
                hbox.add(detailsBtn);
                openEntityBtn.setAlignment(Alignment.MIDDLE_LEFT);
                detailsBtn.setAlignment(Alignment.MIDDLE_RIGHT);
                hbox.setAlignment(Alignment.MIDDLE_LEFT);
                return hbox;
            }
        });

        fieldGroup.addCustomField("entityName", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                entityNameLookup = componentsFactory.createComponent(LookupField.class);
                Collection<MetaClass> persistentMetaClasses = metadata.getTools().getAllPersistentMetaClasses();
                entityNameLookup.setOptionsList(new ArrayList<>(persistentMetaClasses));
                entityNameLookup.setValue(metadata.getClass(getItem().getEntityName()));

                entityNameLookup.addValueChangeListener(e -> {
                    if (e.getValue() != null) {
                        MetaClass metaClass = (MetaClass) e.getValue();
                        getItem().setEntityName(metaClass.getName());
                        fieldGroup.setFieldValue("entityId", null);
                        entityIdPickerField.setMetaClass(metaClass);
                        entityIdPickerField.setEditable(false);
                    } else {
                        getItem().setEntityName(null);
                    }
                    fieldGroup.setFieldValue("entityId", null);
                    entityIdPickerField.setEditable(e.getValue() != null);
                });

                return entityNameLookup;
            }
        });
        fieldGroup.setVisible("entityName", false);

        fieldGroup.addCustomField("entityId", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                entityIdPickerField = componentsFactory.createComponent(PickerField.class);
                entityIdPickerField.addLookupAction();
                entityIdPickerField.addOpenAction();
                entityIdPickerField.addClearAction();

                UUID entityId = getItem().getEntityId();
                String entityName = getItem().getEntityName();
                MetaClass metaClass = metadata.getClass(entityName);
                entityIdPickerField.setMetaClass(metaClass);
                entityIdPickerField.setEditable(metaClass != null);

                if (entityId != null && metaClass != null) {
                    Entity entity = dataManager.load(new LoadContext(metaClass).setId(entityId));
                    entityIdPickerField.setValue(entity);
                }

                entityIdPickerField.addValueChangeListener(e -> {
                    UUID entityId1 = e.getValue() == null ? null : ((HasUuid) e.getValue()).getUuid();
                    getItem().setEntityId(entityId1);
                    initOpenEntityBtn();
                });

                return entityIdPickerField;
            }
        });
        fieldGroup.setVisible("entityId", false);
    }

    protected void initOpenEntityBtn() {
        final Entity entity = findEntity();
        openEntityBtn.setCaption(entity == null ? getMessage("entityNotDefined") : entity.getInstanceName());
        openEntityBtn.setAction(new BaseAction("openEntity") {
            @Override
            public void actionPerform(Component component) {
                if (entity == null) return;
                String entityEditorName = getItem().getEntityEditorName();
                if (Strings.isNullOrEmpty(entityEditorName)) {
                    entityEditorName = getItem().getEntityName() + ".edit";
                }
                openEditor(entityEditorName, entity, WindowManager.OpenType.THIS_TAB);
            }
        });
    }

    @Nullable
    protected Entity findEntity() {
        Entity entity = null;
        UUID entityId = getItem().getEntityId();
        String entityName = getItem().getEntityName();
        if (entityId != null && !Strings.isNullOrEmpty(entityName)) {
            MetaClass metaClass = metadata.getClass(entityName);
            if (metaClass != null) {
                entity = dataManager.load(new LoadContext(metaClass).setId(entityId));
            }
        }
        return entity;
    }

    protected void initProcActors(ProcDefinition procDefinition) {
        for (ProcRole procRole : procDefinition.getProcRoles()) {
            ProcActor procActor = metadata.create(ProcActor.class);
            procActor.setProcInstance(getItem());
            procActor.setProcRole(procRole);
            procActorsDs.addItem(procActor);
        }
    }

    protected class MessageAndCloseAfterActionListener implements ProcAction.AfterActionListener {

        protected String message;

        public MessageAndCloseAfterActionListener(String message) {
            this.message = message;
        }

        @Override
        public void actionCompleted() {
            showNotification(message, NotificationType.HUMANIZED);
            close(COMMIT_ACTION_ID);
        }
    }

    protected class CommitEditorBeforeActionPredicate implements ProcAction.BeforeActionPredicate {

        @Override
        public boolean evaluate() {
            return commit();
        }
    }
}