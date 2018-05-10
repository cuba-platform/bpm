/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.procactions;

import com.haulmont.bpm.BpmConstants;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.action.*;
import com.haulmont.bpm.service.BpmEntitiesService;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.ReferenceToEntitySupport;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * <p>This frame is used for displaying process actions available for the current user.
 * Depending of the process instance associated with the frame, the frame may display buttons that start the process,
 * complete process actions or claim the process task.</p>
 * <p>
 * The frame must be initialized with the {@link Initializer} instance. Use the {@link #initializer()} method to get the
 * instance of the initializer object. After setting all required listeners and predicates you can initialize the frame
 * in two ways:
 *
 * <ul>
 * <li>With the code of {@link ProcDefinition} and entity reference using the {@link Initializer#init(String, Entity)}
 * method</li>
 * <li>With the {@link ProcInstance} object using the  {@link Initializer#init(ProcInstance)} method</li>
 * </ul>
 * See the {@link Initializer#init(String, Entity)} and {@link Initializer#init(ProcInstance)} methods for details
 */
public class ProcActionsFrame extends AbstractFrame {

    private static final Logger log = LoggerFactory.getLogger(ProcActionsFrame.class);

    @Inject
    protected DataManager dataManager;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected ProcessFormService processFormService;

    @Inject
    protected Label noActionsAvailableLbl;

    @Inject
    protected Label taskName;

    @Inject
    protected Label taskStartDate;

    @Inject
    protected GridLayout taskInfoGrid;

    @Inject
    protected BoxLayout actionsBox;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;

    @Inject
    protected BpmEntitiesService bpmEntitiesService;

    protected ProcTask procTask;
    protected ProcInstance procInstance;
    protected String buttonWidth = "100%";

    protected ProcAction.BeforeActionPredicate beforeStartProcessPredicate;
    protected ProcAction.BeforeActionPredicate beforeCompleteTaskPredicate;
    protected ProcAction.BeforeActionPredicate beforeClaimTaskPredicate;
    protected ProcAction.BeforeActionPredicate beforeCancelProcessPredicate;

    protected ProcAction.AfterActionListener afterStartProcessListener;
    protected ProcAction.AfterActionListener afterCompleteTaskListener;
    protected ProcAction.AfterActionListener afterClaimTaskListener;
    protected ProcAction.AfterActionListener afterCancelProcessListener;

    protected Supplier<Map<String, Object>> startProcessActionProcessVariablesSupplier;
    protected Supplier<Map<String, Object>> completeTaskActionProcessVariablesSupplier;
    protected Supplier<Map<String, Object>> startProcessActionScreenParametersSupplier;
    protected Supplier<Map<String, Object>> completeTaskActionScreenParametersSupplier;

    protected boolean startProcessEnabled = true;
    protected boolean cancelProcessEnabled = false;
    protected boolean completeTaskEnabled = true;
    protected boolean claimTaskEnabled = true;
    protected boolean taskInfoEnabled = true;

    protected StartProcessAction startProcessAction;
    protected CancelProcessAction cancelProcessAction;
    protected ClaimProcTaskAction claimProcTaskAction;
    protected List<CompleteProcTaskAction> completeProcTaskActions = new ArrayList<>();

    protected Button cancelProcessBtn;
    private Button startProcessBtn;

    /**
     * @see Initializer#init(String, Entity)
     */
    protected void init(String procCode, Entity entity) {
        ProcDefinition procDefinition = bpmEntitiesService.findProcDefinitionByCode(procCode, BpmConstants.Views.PROC_DEFINITION_WITH_ROLES);
        if (procDefinition == null) {
            log.debug("Process definition with code '{}' not found", procCode);
            return;
        }
        List<ProcInstance> procInstances = bpmEntitiesService.findActiveProcInstancesForEntity(procDefinition.getCode(), entity, BpmConstants.Views.PROC_INSTANCE_FULL);
        procInstance = procInstances.isEmpty() ? null : procInstances.get(0);
        if (procInstance == null) {
            procInstance = metadata.create(ProcInstance.class);
            procInstance.setProcDefinition(procDefinition);
            procInstance.setObjectEntityId(referenceToEntitySupport.getReferenceId(entity));
            procInstance.setEntityName(entity.getMetaClass().getName());
        }
        init(procInstance);
    }

    protected void init(ProcInstance procInstance) {
        this.procInstance = procInstance;
        reset();
        List<ProcTask> procTasks = bpmEntitiesService.findActiveProcTasksForCurrentUser(procInstance, BpmConstants.Views.PROC_TASK_COMPLETE);
        procTask = procTasks.isEmpty() ? null : procTasks.get(0);
        if (procTask == null) {
            if (startProcessEnabled && startProcessAllowed())
                initStartProcessUI();
        } else if (procTask.getProcActor() != null) {
            if (completeTaskEnabled)
                initCompleteTaskUI();
        } else {
            if (claimTaskEnabled)
                initClaimTaskUI();
        }
        if (cancelProcessEnabled && cancelProcessAllowed())
            initCancelAction();
    }

    @Nullable
    public StartProcessAction getStartProcessAction() {
        return startProcessAction;
    }

    public List<CompleteProcTaskAction> getCompleteProcTaskActions() {
        return completeProcTaskActions;
    }

    @Nullable
    public ClaimProcTaskAction getClaimProcTaskAction() {
        return claimProcTaskAction;
    }

    @Nullable
    public CancelProcessAction getCancelProcessAction() {
        return cancelProcessAction;
    }

    /**
     * Method adds a button to the end of the action buttons box. The new button will be displayed below automatically
     * generated action buttons
     */
    public void addActionButton(Button button) {
        actionsBox.add(button);
    }

    protected void reset() {
        startProcessAction = null;
        cancelProcessAction = null;
        claimProcTaskAction = null;
        completeProcTaskActions.clear();
        actionsBox.removeAll();
        noActionsAvailableLbl.setVisible(true);
        taskInfoGrid.setVisible(false);
    }

    protected void initClaimTaskUI() {
        if (taskInfoEnabled)
            initTaskInfoGrid();
        noActionsAvailableLbl.setVisible(false);
        Button claimTaskBtn = componentsFactory.createComponent(Button.class);
        claimTaskBtn.setWidth(buttonWidth);
        claimProcTaskAction = new ClaimProcTaskAction(procTask);
        claimProcTaskAction.addBeforeActionPredicate(beforeClaimTaskPredicate);
        claimProcTaskAction.addAfterActionListener(afterClaimTaskListener);
        claimTaskBtn.setAction(claimProcTaskAction);
        actionsBox.add(claimTaskBtn);
    }

    protected void initCompleteTaskUI() {
        if (taskInfoEnabled)
            initTaskInfoGrid();
        noActionsAvailableLbl.setVisible(false);
        Map<String, ProcFormDefinition> outcomesWithForms = processFormService.getOutcomesWithForms(procTask);
        if (!outcomesWithForms.isEmpty()) {
            for (Map.Entry<String, ProcFormDefinition> entry : outcomesWithForms.entrySet()) {
                CompleteProcTaskAction action = new CompleteProcTaskAction(procTask, entry.getKey(), entry.getValue());
                completeProcTaskActions.add(action);
            }
        } else {
            ProcFormDefinition form = processFormService.getDefaultCompleteTaskForm(procInstance.getProcDefinition());
            CompleteProcTaskAction action = new CompleteProcTaskAction(procTask, BpmConstants.DEFAULT_TASK_OUTCOME, form);
            action.setCaption(getMessage("completeTask"));
            completeProcTaskActions.add(action);
        }

        for (CompleteProcTaskAction completeProcTaskAction : completeProcTaskActions) {
            completeProcTaskAction.addBeforeActionPredicate(beforeCompleteTaskPredicate);
            completeProcTaskAction.addAfterActionListener(afterCompleteTaskListener);
            completeProcTaskAction.setProcessVariablesSupplier(completeTaskActionProcessVariablesSupplier);
            completeProcTaskAction.setScreenParametersSupplier(completeTaskActionScreenParametersSupplier);
            Button actionBtn = componentsFactory.createComponent(Button.class);
            actionBtn.setWidth(buttonWidth);
            actionBtn.setAction(completeProcTaskAction);
            actionsBox.add(actionBtn);
        }
    }

    protected void initStartProcessUI() {
        taskInfoGrid.setVisible(false);
        noActionsAvailableLbl.setVisible(false);
        startProcessBtn = componentsFactory.createComponent(Button.class);
        startProcessBtn.setWidth(buttonWidth);
        startProcessAction = new StartProcessAction(procInstance);
        startProcessAction.addBeforeActionPredicate(beforeStartProcessPredicate);
        startProcessAction.addAfterActionListener(afterStartProcessListener);
        startProcessAction.setProcessVariablesSupplier(startProcessActionProcessVariablesSupplier);
        startProcessAction.setScreenParametersSupplier(startProcessActionScreenParametersSupplier);
        startProcessBtn.setAction(startProcessAction);
        actionsBox.add(startProcessBtn);
    }

    protected void initCancelAction() {
        cancelProcessBtn = componentsFactory.createComponent(Button.class);
        cancelProcessBtn.setWidth(buttonWidth);
        cancelProcessAction = new CancelProcessAction(procInstance);
        cancelProcessAction.addBeforeActionPredicate(beforeCancelProcessPredicate);
        cancelProcessAction.addAfterActionListener(afterCancelProcessListener);
        cancelProcessBtn.setAction(cancelProcessAction);
        actionsBox.add(cancelProcessBtn);
    }

    protected void initTaskInfoGrid() {
        taskInfoGrid.setVisible(true);
        taskName.setValue(procTask.getLocName());
        taskStartDate.setValue(procTask.getStartDate());
    }

    protected boolean cancelProcessAllowed() {
        return procInstance.getStartDate() != null
                && procInstance.getEndDate() == null;
    }

    protected boolean startProcessAllowed() {
        return procInstance.getStartDate() == null;
    }

    /**
     * Returns a new instance of frame initializer
     */
    public Initializer initializer() {
        return new Initializer();
    }

    /**
     * Class is used to initialize the frame.
     */
    public class Initializer {

        protected ProcAction.BeforeActionPredicate beforeStartProcessPredicate;
        protected ProcAction.BeforeActionPredicate beforeCompleteTaskPredicate;
        protected ProcAction.BeforeActionPredicate beforeClaimTaskPredicate;
        protected ProcAction.BeforeActionPredicate beforeCancelProcessPredicate;

        protected ProcAction.AfterActionListener afterStartProcessListener;
        protected ProcAction.AfterActionListener afterCompleteTaskListener;
        protected ProcAction.AfterActionListener afterClaimTaskListener;
        protected ProcAction.AfterActionListener afterCancelProcessListener;

        protected Supplier<Map<String, Object>> startProcessActionProcessVariablesSupplier;
        protected Supplier<Map<String, Object>> completeTaskActionProcessVariablesSupplier;

        protected Supplier<Map<String, Object>> startProcessActionScreenParametersSupplier;
        protected Supplier<Map<String, Object>> completeTaskActionScreenParametersSupplier;

        protected boolean startProcessEnabled = true;
        protected boolean cancelProcessEnabled = false;
        protected boolean completeTaskEnabled = true;
        protected boolean claimTaskEnabled = true;
        protected boolean taskInfoEnabled = true;

        protected String buttonWidth = "100%";

        private Initializer() {
        }

        public Initializer setBeforeStartProcessPredicate(ProcAction.BeforeActionPredicate predicate) {
            this.beforeStartProcessPredicate = predicate;
            return this;
        }

        public Initializer setBeforeCompleteTaskPredicate(ProcAction.BeforeActionPredicate beforeCompleteTaskPredicate) {
            this.beforeCompleteTaskPredicate = beforeCompleteTaskPredicate;
            return this;
        }

        public Initializer setBeforeClaimTaskPredicate(ProcAction.BeforeActionPredicate beforeClaimTaskPredicate) {
            this.beforeClaimTaskPredicate = beforeClaimTaskPredicate;
            return this;
        }

        public Initializer setBeforeCancelProcessPredicate(ProcAction.BeforeActionPredicate beforeCancelProcessPredicate) {
            this.beforeCancelProcessPredicate = beforeCancelProcessPredicate;
            return this;
        }

        public Initializer setAfterStartProcessListener(ProcAction.AfterActionListener afterStartProcessListener) {
            this.afterStartProcessListener = afterStartProcessListener;
            return this;
        }

        public Initializer setAfterCompleteTaskListener(ProcAction.AfterActionListener afterCompleteTaskListener) {
            this.afterCompleteTaskListener = afterCompleteTaskListener;
            return this;
        }

        public Initializer setAfterClaimTaskListener(ProcAction.AfterActionListener afterClaimTaskListener) {
            this.afterClaimTaskListener = afterClaimTaskListener;
            return this;
        }

        public Initializer setAfterCancelProcessListener(ProcAction.AfterActionListener afterCancelProcessListener) {
            this.afterCancelProcessListener = afterCancelProcessListener;
            return this;
        }

        public Initializer setStartProcessEnabled(boolean startProcessEnabled) {
            this.startProcessEnabled = startProcessEnabled;
            return this;
        }

        public Initializer setCancelProcessEnabled(boolean cancelProcessEnabled) {
            this.cancelProcessEnabled = cancelProcessEnabled;
            return this;
        }

        public Initializer setCompleteTaskEnabled(boolean completeTaskEnabled) {
            this.completeTaskEnabled = completeTaskEnabled;
            return this;
        }

        public Initializer setClaimTaskEnabled(boolean claimTaskEnabled) {
            this.claimTaskEnabled = claimTaskEnabled;
            return this;
        }

        public Initializer setTaskInfoEnabled(boolean taskInfoEnabled) {
            this.taskInfoEnabled = taskInfoEnabled;
            return this;
        }

        public Initializer setButtonWidth(String buttonWidth) {
            this.buttonWidth = buttonWidth;
            return this;
        }

        /**
         * Sets the process variables {@link Supplier} for the start process action. The supplier should return a map
         * with the process variables which will be set to the Activiti process instance when the {@link
         * StartProcessAction} is performed.
         */
        public Initializer setStartProcessActionProcessVariablesSupplier(Supplier<Map<String, Object>> startProcessActionProcessVariablesSupplier) {
            this.startProcessActionProcessVariablesSupplier = startProcessActionProcessVariablesSupplier;
            return this;
        }

        /**
         * Sets the process variables {@link Supplier} for all complete task actions. The supplier should return a map
         * with the process variables which will be set to the Activiti process instance when the {@link
         * CompleteProcTaskAction} is performed. If you need different variables suppliers for different actions, then
         * you may get a list of actions with the {@link #getCompleteProcTaskActions()}, find the required action and
         * use the {@link ProcAction#setProcessVariablesSupplier(Supplier)} method.
         */
        public Initializer setCompleteTaskActionProcessVariablesSupplier(Supplier<Map<String, Object>> completeTaskActionProcessVariablesSupplier) {
            this.completeTaskActionProcessVariablesSupplier = completeTaskActionProcessVariablesSupplier;
            return this;
        }

        /**
         * Sets the screen parameters {@link Supplier} for the process form of the start process action. The supplier
         * should return a map with the screen parameters which will be passed to the process from screen.
         */
        public Initializer setStartProcessActionScreenParametersSupplier(Supplier<Map<String, Object>> startProcessActionScreenParametersSupplier) {
            this.startProcessActionScreenParametersSupplier = startProcessActionScreenParametersSupplier;
            return this;
        }

        /**
         * Sets the screen parameters {@link Supplier} for the process form of complete task actions. The supplier
         * should return a map with the screen parameters which will be passed to the process from screen.
         */
        public Initializer setCompleteTaskActionScreenParametersSupplier(Supplier<Map<String, Object>> completeTaskActionScreenParametersSupplier) {
            this.completeTaskActionScreenParametersSupplier = completeTaskActionScreenParametersSupplier;
            return this;
        }

        /**
         * Performs the standard initialization:
         * <ul><li>Commits the editor before each action is applied</li><li>shows notification and re-intializes the
         * procActionsFrame after the action is applied</li></ul>
         */
        public Initializer standard() {
            Window window = ComponentsHelper.getWindow(ProcActionsFrame.this);
            final Window.Editor editor;
            if (window instanceof Window.Editor) {
                editor = (Window.Editor) window;
            } else {
                throw new BpmException("ProcActionsFrame.standard() must be used inside Window.Editor only");
            }

            this.beforeStartProcessPredicate = editor::commit;
            this.beforeCompleteTaskPredicate = editor::commit;
            this.beforeClaimTaskPredicate = editor::commit;
            this.beforeCancelProcessPredicate = editor::commit;
            this.afterStartProcessListener = () -> {
                editor.showNotification(getMessage("processStarted"));
                ProcInstance reloadedProcInstance = dataManager.reload(ProcActionsFrame.this.procInstance, BpmConstants.Views.PROC_INSTANCE_FULL);
                ProcActionsFrame.this.init(reloadedProcInstance);
            };
            this.afterCompleteTaskListener = () -> {
                editor.showNotification(getMessage("taskCompleted"));
                ProcInstance reloadedProcInstance = dataManager.reload(ProcActionsFrame.this.procInstance, BpmConstants.Views.PROC_INSTANCE_FULL);
                ProcActionsFrame.this.init(reloadedProcInstance);
            };
            this.afterClaimTaskListener = () -> {
                editor.showNotification(getMessage("taskClaimed"));
                ProcInstance reloadedProcInstance = dataManager.reload(ProcActionsFrame.this.procInstance, BpmConstants.Views.PROC_INSTANCE_FULL);
                ProcActionsFrame.this.init(reloadedProcInstance);
            };
            this.afterCancelProcessListener = () -> {
                editor.showNotification(getMessage("processCancelled"));
                ProcInstance reloadedProcInstance = dataManager.reload(ProcActionsFrame.this.procInstance, BpmConstants.Views.PROC_INSTANCE_FULL);
                ProcActionsFrame.this.init(reloadedProcInstance);
            };
            return this;
        }

        /**
         * The method tries to find a process instance by the specified process code and the entity reference. If the
         * process instance is not found then a new one is created. Then the UI for available actions for the current
         * user and the process instance is initialized.
         *
         * @param processCode process definition code
         * @param entity      an entity
         */
        public void init(String processCode, Entity entity) {
            copyFields();
            ProcActionsFrame.this.init(processCode, entity);
        }

        /**
         * Method initializes the UI for actions which are available for the current user with the specified process
         * instance.
         *
         * @param procInstance a process instance
         */
        public void init(ProcInstance procInstance) {
            copyFields();
            ProcActionsFrame.this.init(procInstance);
        }

        protected void copyFields() {
            ProcActionsFrame.this.beforeStartProcessPredicate = this.beforeStartProcessPredicate;
            ProcActionsFrame.this.afterStartProcessListener = this.afterStartProcessListener;
            ProcActionsFrame.this.beforeCompleteTaskPredicate = this.beforeCompleteTaskPredicate;
            ProcActionsFrame.this.afterCompleteTaskListener = this.afterCompleteTaskListener;
            ProcActionsFrame.this.beforeClaimTaskPredicate = this.beforeClaimTaskPredicate;
            ProcActionsFrame.this.afterClaimTaskListener = this.afterClaimTaskListener;
            ProcActionsFrame.this.beforeCancelProcessPredicate = this.beforeCancelProcessPredicate;
            ProcActionsFrame.this.afterCancelProcessListener = this.afterCancelProcessListener;
            ProcActionsFrame.this.startProcessEnabled = this.startProcessEnabled;
            ProcActionsFrame.this.cancelProcessEnabled = this.cancelProcessEnabled;
            ProcActionsFrame.this.completeTaskEnabled = this.completeTaskEnabled;
            ProcActionsFrame.this.claimTaskEnabled = this.claimTaskEnabled;
            ProcActionsFrame.this.taskInfoEnabled = this.taskInfoEnabled;
            ProcActionsFrame.this.buttonWidth = this.buttonWidth;
            ProcActionsFrame.this.startProcessActionProcessVariablesSupplier = this.startProcessActionProcessVariablesSupplier;
            ProcActionsFrame.this.completeTaskActionProcessVariablesSupplier = this.completeTaskActionProcessVariablesSupplier;
            ProcActionsFrame.this.startProcessActionScreenParametersSupplier = this.startProcessActionScreenParametersSupplier;
            ProcActionsFrame.this.completeTaskActionScreenParametersSupplier = this.completeTaskActionScreenParametersSupplier;
        }
    }

    public ProcInstance getProcInstance() {
        return procInstance;
    }
}
