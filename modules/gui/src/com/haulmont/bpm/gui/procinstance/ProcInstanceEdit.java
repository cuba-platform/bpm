/*
 * Copyright (c) 2015 com.haulmont.bpm.gui.procinstance
 */
package com.haulmont.bpm.gui.procinstance;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.*;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.gui.procactor.ProcActorsFrame;
import com.haulmont.bpm.gui.procattachment.ProcAttachmentsFrame;
import com.haulmont.bpm.gui.proctaskactions.ProcTaskActionsFrame;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
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
import org.apache.commons.lang.BooleanUtils;

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
    protected ProcessRuntimeService processRuntimeService;

    @Inject
    protected ProcessFormService processFormService;

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

    @Inject
    protected ProcessMessagesService processMessagesService;

    @Inject
    protected Table procTasksTable;

    @Named("fieldGroup.procDefinition")
    protected LookupField procDefinitionLookup;

    @Inject
    protected ProcTaskActionsFrame procTaskActionsFrame;

    @Inject
    protected CollectionDatasource<ProcTask, UUID> procTasksDs;

    @Inject
    protected UserSession userSession;


    @Override
    public void setItem(Entity item) {
        super.setItem(item);

        setShowSaveNotification(false);

        procTasksDs.refresh();
        initProcTaskActionsFrame();

        if (getItem().getStartDate() == null) {
            procTaskActionsFrame.addProcessAction(new StartProcessAction());
        }

        if (BooleanUtils.isTrue(getItem().getActive())) {
            procTaskActionsFrame.addProcessAction(new CancelProcessAction());
        }

        if (getItem().getStartDate() != null) {
            procDefinitionLookup.setEditable(false);
        }

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

        addFieldGroupCustomFields();

//        procTasksTable.addGeneratedColumn("outcome", new Table.ColumnGenerator<ProcTask>() {
//            @Override
//            public Component generateCell(ProcTask procTask) {
//                if (Strings.isNullOrEmpty(procTask.getOutcome())) return null;
//                Label label = componentsFactory.createComponent(Label.class);
//                ProcDefinition procDefinition = procTask.getProcInstance().getProcDefinition();
//                String key = procTask.getName() + "." + procTask.getOutcome();
//                label.setValue(processMessagesService.getMessage(procDefinition.getActId(), key));
//                return label;
//            }
//        });

        procActorsFrame.setProcInstance(getItem());
        procActorsFrame.refresh();

        procAttachmentsFrame.setProcInstance(getItem());
        procAttachmentsFrame.refresh();
    }

    @Override
    protected void postInit() {
        super.postInit();
    }

    protected void initProcTaskActionsFrame() {
        procTaskActionsFrame.removeAllActions();

        for (ProcTask procTask : procTasksDs.getItems()) {
            if (userHasActionsOnTask(procTask)) {
                procTaskActionsFrame.addProcTaskActions(procTask);
                break;
            }
        }

        procTaskActionsFrame.addActionListener(new ProcTaskActionsFrame.ActionListener() {
            @Override
            public void actionPerformed() {
                showNotification(getMessage("taskCompleted"), NotificationType.HUMANIZED);
                close(COMMIT_ACTION_ID);
            }
        });
    }

    protected boolean userHasActionsOnTask(ProcTask procTask) {
        User user = userSession.getCurrentOrSubstitutedUser();
        boolean taskIsActive = procTask.getEndDate() == null;
        boolean taskAssignedForUser = procTask.getProcActor() != null
                && user.equals(procTask.getProcActor().getUser());
        boolean userIsCandidateForTask = procTask.getProcActor() == null
                && procTask.getCandidateUsers() != null
                && procTask.getCandidateUsers().contains(user);

        return taskIsActive && (taskAssignedForUser || userIsCandidateForTask);
    }

    private void addFieldGroupCustomFields() {
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
                        PickerField entityIdField = (PickerField) fieldGroup.getFieldComponent("entityId");
                        entityIdField.setMetaClass(metaClass);
                        entityIdField.setEditable(value != null);
                    }
                });
                return component;
            }
        });

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
                    }
                });

                return component;
            }
        });
    }

    protected void initProcActors(ProcDefinition procDefinition) {
        for (ProcRole procRole : procDefinition.getProcRoles()) {
            ProcActor procActor = metadata.create(ProcActor.class);
            procActor.setProcInstance(getItem());
            procActor.setProcRole(procRole);
            procActorsDs.addItem(procActor);
        }
    }


    protected class StartProcessAction extends BaseAction {

        protected StartProcessAction() {
            super("startProcess");
        }

        @Override
        public void actionPerform(Component component) {
            if (commit()) {
                ProcFormDefinition startForm = processFormService.getStartForm(getItem().getProcDefinition());
                if (startForm != null) {
                    Map<String, Object> formParams = new HashMap<>();
                    formParams.put("procInstance", getItem());
                    formParams.put("formDefinition", startForm);
                    final Window window = openWindow(startForm.getName(), WindowManager.OpenType.DIALOG, formParams);
                    window.addListener(new CloseListener() {
                        @Override
                        public void windowClosed(String actionId) {
                            if (COMMIT_ACTION_ID.equals(actionId)) {
                                String comment = null;
                                Map<String, Object> formResult = null;
                                if (window instanceof ProcForm) {
                                    comment = ((ProcForm) window).getComment();
                                    formResult = ((ProcForm) window).getFormResult();
                                }
                                _startProcess(comment, formResult);
                            }
                        }
                    });
                } else {
                    _startProcess(null, new HashMap<String, Object>());
                }
            }
        }

        protected void _startProcess(String startComment, Map<String, Object> processVariables) {
            processRuntimeService.startProcess(getItem(),startComment, processVariables);
            showNotification(getMessage("processStarted"), NotificationType.HUMANIZED);
            close(COMMIT_ACTION_ID);
        }


        @Override
        public String getCaption() {
            return getMessage("startProcess");
        }
    }

    protected class CancelProcessAction extends BaseAction {

        protected CancelProcessAction() {
            super("cancelProcess");
        }

        @Override
        public void actionPerform(Component component) {
            if (commit()) {
                Map<String, Object> formParams = new HashMap<>();
                ProcFormDefinition cancelForm = processFormService.getCancelForm(getItem().getProcDefinition());
                formParams.put("formDefinition", cancelForm);
                final Window cancelProcessForm = openWindow(cancelForm.getName(), WindowManager.OpenType.DIALOG, formParams);
                cancelProcessForm.addListener(new CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        if (COMMIT_ACTION_ID.equals(actionId)) {
                            String comment = null;
                            if (cancelProcessForm instanceof ProcForm) {
                                comment = ((ProcForm) cancelProcessForm).getComment();
                            }
                            ProcInstance reloadedProcInstance = processRuntimeService.cancelProcess(getItem(), comment);
                            setItem(reloadedProcInstance);
                        }
                    }
                });
            }
        }

        @Override
        public String getCaption() {
            return getMessage("cancelProcess");
        }
    }
}