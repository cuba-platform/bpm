/*
 * Copyright (c) 2015 com.haulmont.bpm.gui.procinstance
 */
package com.haulmont.bpm.gui.procinstance;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.*;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.action.ProcTaskAction;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.gui.procactor.ProcActorsFrame;
import com.haulmont.bpm.gui.procattachment.ProcAttachmentsFrame;
import com.haulmont.bpm.gui.proctaskactions.ProcTaskActionsFrame;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author gorbunkov
 */
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

    @Named("fieldGroup.entityName")
    protected LookupField entityNameLookup;

    @Named("fieldGroup.entityId")
    protected PickerField entityIdPickerField;

    @Named("fieldGroup.entityEditorName")
    protected TextField entityEditorNameField;

    @Inject
    protected ProcTaskActionsFrame procTaskActionsFrame;

    @Inject
    protected CollectionDatasource<ProcTask, UUID> procTasksDs;

    @Inject
    protected UserSession userSession;

    protected boolean entityDetailsVisible = false;
    protected LinkButton openEntityBtn;

    @Override
    public void setItem(Entity item) {
        super.setItem(item);

//        setShowSaveNotification(false);
        procTasksDs.refresh();

        setComponentsVisible();
        setComponentsEditable();

        addFieldGroupCustomFields();

        procInstanceDs.addListener(new DsListenerAdapter<ProcInstance>() {
            @Override
            public void valueChanged(ProcInstance source, String property, Object prevValue, Object value) {
                if ("procDefinition".equals(property)) {
                    procActorsFrame.setProcInstance(getItem());
                    procActorsFrame.refresh();
                    initProcActors((ProcDefinition) value);
                }
            }
        });

        procActorsFrame.setProcInstance(getItem());
        procActorsFrame.refresh();

        procAttachmentsFrame.setProcInstance(getItem());
        procAttachmentsFrame.refresh();

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
        procTaskActionsFrame.setBeforeStartProcessPredicate(new ProcTaskAction.BeforeActionPredicate() {
            @Override
            public boolean evaluate() {
                if (PersistenceHelper.isNew(getItem())) {
                    showNotification(getMessage("saveProcInstance"), NotificationType.WARNING);
                    return false;
                } else {
                    return commit();
                }
            }
        });
        procTaskActionsFrame.setAfterStartProcessListener(new MessageAndCloseAfterActionListener(getMessage("processStarted")));
        procTaskActionsFrame.setAfterCancelProcessListener(new MessageAndCloseAfterActionListener(getMessage("processCancelled")));
        procTaskActionsFrame.setAfterCompleteTaskListener(new MessageAndCloseAfterActionListener(getMessage("taskCompleted")));
        procTaskActionsFrame.setAfterClaimTaskListener(new MessageAndCloseAfterActionListener(getMessage("taskClaimed")));

        procTaskActionsFrame.setBeforeCompleteTaskPredicate(new CommitEditorBeforeActionPredicate());
        procTaskActionsFrame.setBeforeClaimTaskPredicate(new CommitEditorBeforeActionPredicate());
        procTaskActionsFrame.setBeforeCancelProcessPredicate(new CommitEditorBeforeActionPredicate());

        procTaskActionsFrame.init(getItem());
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
                detailsBtn.setAlignment(Alignment.MIDDLE_RIGHT);
                hbox.expand(openEntityBtn);
                hbox.setAlignment(Alignment.MIDDLE_LEFT);
                return hbox;
            }
        });

        fieldGroup.addCustomField("entityName", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                LookupField component = componentsFactory.createComponent(LookupField.class);
                Collection<MetaClass> persistentMetaClasses = metadata.getTools().getAllPersistentMetaClasses();
                component.setOptionsList(new ArrayList<>(persistentMetaClasses));
                component.setValue(metadata.getClass(getItem().getEntityName()));

                component.addListener(new ValueListener() {
                    @Override
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        MetaClass metaClass = (MetaClass) value;
                        getItem().setEntityName(metaClass.getName());
                        fieldGroup.setFieldValue("entityId", null);
                        entityIdPickerField.setMetaClass(metaClass);
                        entityIdPickerField.setEditable(value != null);
                    }
                });
                return component;
            }
        });
        fieldGroup.setVisible("entityName", false);

        fieldGroup.addCustomField("entityId", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                PickerField component = componentsFactory.createComponent(PickerField.class);
                component.addLookupAction();
                component.addOpenAction();
                component.addClearAction();
                component.setMetaClass((MetaClass) fieldGroup.getFieldValue("entityName"));

                UUID entityId = getItem().getEntityId();
                String entityName = getItem().getEntityName();
                if (entityId != null && !Strings.isNullOrEmpty(entityName)) {
                    MetaClass metaClass = metadata.getClass(entityName);
                    if (metaClass != null) {
                        Entity entity = dataManager.load(new LoadContext(metaClass).setId(entityId));
                        component.setValue(entity);
                    }
                }

                component.addListener(new ValueListener() {
                    @Override
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        UUID entityId = value == null ? null : ((Entity) value).getUuid();
                        getItem().setEntityId(entityId);
                        initOpenEntityBtn();
                    }
                });

                return component;
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

    protected class MessageAndCloseAfterActionListener implements ProcTaskAction.AfterActionListener {

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

    protected class CommitEditorBeforeActionPredicate implements ProcTaskAction.BeforeActionPredicate {

        @Override
        public boolean evaluate() {
            return commit();
        }
    }
}