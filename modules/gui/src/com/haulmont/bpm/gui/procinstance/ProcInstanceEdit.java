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
import com.haulmont.cuba.core.global.*;
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

    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;

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
        procAttachmentsFrame.setProcInstance(getItem());
        procTasksFrame.setProcInstance(getItem());
        procTasksFrame.refresh();
    }

    @Override
    protected void postInit() {
        super.postInit();
        initProcTaskActionsFrame();
    }

    @Override
    protected boolean postCommit(boolean committed, boolean close) {
        boolean result = super.postCommit(committed, close);
        procAttachmentsFrame.putFilesIntoStorage();
        return result;
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
                .setCancelProcessEnabled(true)
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
                        fieldGroup.setVisible("entity", entityDetailsVisible);
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
                        fieldGroup.setFieldValue("entity", null);
                        entityIdPickerField.setMetaClass(metaClass);
                        entityIdPickerField.setEditable(false);
                    } else {
                        getItem().setEntityName(null);
                    }
                    fieldGroup.setFieldValue("entity", null);
                    entityIdPickerField.setEditable(e.getValue() != null);
                });

                return entityNameLookup;
            }
        });
        fieldGroup.setVisible("entityName", false);

        fieldGroup.addCustomField("entity", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                entityIdPickerField = componentsFactory.createComponent(PickerField.class);
                entityIdPickerField.addLookupAction();
                entityIdPickerField.addOpenAction();
                entityIdPickerField.addClearAction();

                Object entityId = getItem().getObjectEntityId();
                String entityName = getItem().getEntityName();
                MetaClass metaClass = metadata.getClass(entityName);
                entityIdPickerField.setMetaClass(metaClass);
                entityIdPickerField.setEditable(metaClass != null);

                if (entityId != null && metaClass != null) {
                    Entity entity = findEntity(entityName, entityId);
                    entityIdPickerField.setValue(entity);
                }

                entityIdPickerField.addValueChangeListener(e -> {
                    Object entityId1 = e.getValue() == null ?
                            null :
                            referenceToEntitySupport.getReferenceId((Entity) e.getValue());
                    getItem().setObjectEntityId(entityId1);
                    initOpenEntityBtn();
                });

                return entityIdPickerField;
            }
        });
        fieldGroup.setVisible("entity", false);
    }

    protected void initOpenEntityBtn() {
        final Entity entity = findEntity(getItem().getEntityName(), getItem().getObjectEntityId());
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
    protected Entity findEntity(String entityName, Object entityId) {
        Entity entity = null;
        if (entityId != null && !Strings.isNullOrEmpty(entityName)) {
            MetaClass metaClass = metadata.getClass(entityName);
            if (metaClass != null) {
                LoadContext ctx = new LoadContext(metaClass).setQuery(
                        LoadContext.createQuery(String.format("select e from %s e where e.%s = :entityId",
                                entityName,
                                referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass)))
                                .setParameter("entityId", entityId))
                        .setView(View.MINIMAL);
                entity = dataManager.load(ctx);
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