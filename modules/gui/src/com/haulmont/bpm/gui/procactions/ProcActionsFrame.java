/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.procactions;

import com.haulmont.bpm.BpmConstants;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.action.*;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.HasUuid;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.global.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>This frame is used for displaying process actions available for the current user.
 * Depending of the process instance associated with the frame, the frame may display buttons
 * that start the process, complete process actions or claim the process task.</p>
 *
 * The frame must be initialized with the {@link Initializer} instance. Use the {@link #initializer()}
 * method to get the instance of the initializer object. After setting all required listeners and predicates you can
 * initialize the frame in two ways:
 *
 * <ul>
 *     <li>With the code of {@link ProcDefinition} and entity reference using the {@link Initializer#init(String, Entity)} method</li>
 *     <li>With the {@link ProcInstance} object using the  {@link Initializer#init(ProcInstance)} method</li>
 * </ul>
 * See the {@link Initializer#init(String, Entity)} and {@link Initializer#init(ProcInstance)} methods for details
 */
public class ProcActionsFrame extends AbstractFrame {

    private final Logger log = LoggerFactory.getLogger(ProcActionsFrame.class);

    @Inject
    protected DataManager dataManager;

    @Inject
    protected UserSession userSession;

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

    protected ProcTask procTask;
    protected ProcInstance procInstance;
    protected String buttonWidth = "150px";

    protected ProcAction.BeforeActionPredicate beforeStartProcessPredicate;
    protected ProcAction.BeforeActionPredicate beforeCompleteTaskPredicate;
    protected ProcAction.BeforeActionPredicate beforeClaimTaskPredicate;
    protected ProcAction.BeforeActionPredicate beforeCancelProcessPredicate;

    protected ProcAction.AfterActionListener afterStartProcessListener;
    protected ProcAction.AfterActionListener afterCompleteTaskListener;
    protected ProcAction.AfterActionListener afterClaimTaskListener;
    protected ProcAction.AfterActionListener afterCancelProcessListener;

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

    /**
     * @see Initializer#init(String, Entity)
     */
    protected void init(String procCode, Entity entity) {
        if (!(entity instanceof HasUuid))
            throw new UnsupportedOperationException("Entity " + entity + " has no persistent UUID attribute");

        ProcDefinition procDefinition = findProcDefinition(procCode);
        if (procDefinition == null) {
            log.debug("Process definition with code '{}' not found", procCode);
            return;
        }
        procInstance = findProcInstance(procDefinition, entity);
        if (procInstance == null) {
            procInstance = metadata.create(ProcInstance.class);
            procInstance.setProcDefinition(procDefinition);
            procInstance.setEntityId(((HasUuid) entity).getUuid());
            procInstance.setEntityName(entity.getMetaClass().getName());
            getDsContext().addBeforeCommitListener(context -> context.getCommitInstances().add(procInstance));
        }

        init(procInstance);
    }

    /**
     * @see Initializer#init(ProcInstance)
     * @deprecated The method will be declared as protected in next platform releases. Use the {@link Initializer}
     * to initialize the frame.
     */
    @Deprecated
    public void init(ProcInstance procInstance) {
        this.procInstance = procInstance;
        reset();
        procTask = findCurrentUserProcTask();
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
        claimProcTaskAction = new ClaimProcTaskAction(procTask, claimTaskBtn);
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
                Button actionBtn = componentsFactory.createComponent(Button.class);
                actionBtn.setWidth(buttonWidth);
                CompleteProcTaskAction action = new CompleteProcTaskAction(procTask, entry.getKey(), entry.getValue(), actionBtn);
                action.addBeforeActionPredicate(beforeCompleteTaskPredicate);
                action.addAfterActionListener(afterCompleteTaskListener);
                actionBtn.setAction(action);
                actionsBox.add(actionBtn);
                completeProcTaskActions.add(action);
            }
        } else {
            Button actionBtn = componentsFactory.createComponent(Button.class);
            actionBtn.setWidth(buttonWidth);
            ProcFormDefinition form = processFormService.getDefaultCompleteTaskForm(procInstance.getProcDefinition());
            CompleteProcTaskAction action = new CompleteProcTaskAction(procTask, BpmConstants.DEFAULT_TASK_OUTCOME, form, actionBtn);
            action.addBeforeActionPredicate(beforeCompleteTaskPredicate);
            action.addAfterActionListener(afterCompleteTaskListener);
            actionBtn.setAction(action);
            actionBtn.setCaption(getMessage("completeTask"));
            actionsBox.add(actionBtn);
            completeProcTaskActions.add(action);
        }
    }

    protected void initStartProcessUI() {
        taskInfoGrid.setVisible(false);
        noActionsAvailableLbl.setVisible(false);
        Button startProcessBtn = componentsFactory.createComponent(Button.class);
        startProcessBtn.setWidth(buttonWidth);
        startProcessAction = new StartProcessAction(procInstance, startProcessBtn);
        startProcessAction.addBeforeActionPredicate(beforeStartProcessPredicate);
        startProcessAction.addAfterActionListener(afterStartProcessListener);
        startProcessBtn.setAction(startProcessAction);
        actionsBox.add(startProcessBtn);
    }

    protected void initCancelAction() {
        cancelProcessBtn = componentsFactory.createComponent(Button.class);
        cancelProcessBtn.setWidth(buttonWidth);
        cancelProcessAction = new CancelProcessAction(procInstance, cancelProcessBtn);
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

    protected ProcTask findCurrentUserProcTask() {
        LoadContext<ProcTask> ctx = new LoadContext<>(ProcTask.class);
        ctx.setQueryString("select pt from bpm$ProcTask pt left join pt.procActor pa left join pa.user pau " +
                "where pt.procInstance.id = :procInstance and (pau.id = :userId or " +
                "(pa is null and exists(select pt2 from bpm$ProcTask pt2 join pt2.candidateUsers cu where pt2.id = pt.id and cu.id = :userId))) " +
                "and pt.endDate is null")
                .setParameter("procInstance", procInstance)
                .setParameter("userId", userSession.getCurrentOrSubstitutedUser());
        ctx.setView("procTask-complete");
        List<ProcTask> result = dataManager.loadList(ctx);
        return result.isEmpty() ? null : result.get(0);
    }

    @Nullable
    protected ProcDefinition findProcDefinition(String processCode) {
        LoadContext ctx = LoadContext.create(ProcDefinition.class);
        ctx.setQueryString("select pd from bpm$ProcDefinition pd where pd.code = :code")
                .setParameter("code", processCode);
        return (ProcDefinition) dataManager.load(ctx);
    }

    @Nullable
    protected ProcInstance findProcInstance(ProcDefinition procDefinition, Entity entity) {
        LoadContext ctx = LoadContext.create(ProcInstance.class).setView("procInstance-start");
        ctx.setQueryString("select pi from bpm$ProcInstance pi where pi.procDefinition.id = :procDefinition and pi.entityId = :entityId")
                .setParameter("procDefinition", procDefinition)
                .setParameter("entityId", entity);
        return (ProcInstance) dataManager.load(ctx);
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

        protected boolean startProcessEnabled = true;
        protected boolean cancelProcessEnabled = false;
        protected boolean completeTaskEnabled = true;
        protected boolean claimTaskEnabled = true;
        protected boolean taskInfoEnabled = true;

        protected String buttonWidth = "150px";

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
         * The method tries to find the process instance by the specified process code and the entity reference.
         * If the process instance is not found then a new one is created.
         * Then the UI for available actions for the current user and the process instance is initialized.
         * @param processCode process definition code
         * @param entity an entity
         */

        public void init(String processCode, Entity entity) {
            copyFields();
            ProcActionsFrame.this.init(processCode, entity);
        }

        /**
         * Method initializes the UI for actions which are available for the current user with the
         * specified process instance.
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
        }
    }

    public ProcInstance getProcInstance() {
        return procInstance;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public ProcAction.AfterActionListener getAfterStartProcessListener() {
        return afterStartProcessListener;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setAfterStartProcessListener(ProcAction.AfterActionListener listener) {
        this.afterStartProcessListener = listener;
        if (startProcessAction != null) startProcessAction.addAfterActionListener(listener);
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public ProcAction.AfterActionListener getAfterCompleteTaskListener() {
        return afterCompleteTaskListener;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setAfterCompleteTaskListener(ProcAction.AfterActionListener listener) {
        this.afterCompleteTaskListener = listener;
        completeProcTaskActions.stream().forEach(action ->
                action.addAfterActionListener(listener));
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public ProcAction.BeforeActionPredicate getBeforeStartProcessPredicate() {
        return beforeStartProcessPredicate;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setBeforeStartProcessPredicate(ProcAction.BeforeActionPredicate predicate) {
        this.beforeStartProcessPredicate = predicate;
        if (startProcessAction != null)
            startProcessAction.addBeforeActionPredicate(predicate);
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public ProcAction.AfterActionListener getAfterClaimTaskListener() {
        return afterClaimTaskListener;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setAfterClaimTaskListener(ProcAction.AfterActionListener listener) {
        this.afterClaimTaskListener = listener;
        if (claimProcTaskAction != null)
            claimProcTaskAction.addAfterActionListener(listener);
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public ProcAction.AfterActionListener getAfterCancelProcessListener() {
        return afterCancelProcessListener;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setAfterCancelProcessListener(ProcAction.AfterActionListener listener) {
        this.afterCancelProcessListener = listener;
        if (cancelProcessAction != null)
            cancelProcessAction.addAfterActionListener(listener);
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public ProcAction.BeforeActionPredicate getBeforeCompleteTaskPredicate() {
        return beforeCompleteTaskPredicate;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setBeforeCompleteTaskPredicate(ProcAction.BeforeActionPredicate predicate) {
        this.beforeCompleteTaskPredicate = predicate;
        completeProcTaskActions.stream().forEach(action -> action.addBeforeActionPredicate(predicate));
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public ProcAction.BeforeActionPredicate getBeforeClaimTaskPredicate() {
        return beforeClaimTaskPredicate;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setBeforeClaimTaskPredicate(ProcAction.BeforeActionPredicate predicate) {
        this.beforeClaimTaskPredicate = predicate;
        if (claimProcTaskAction != null)
            claimProcTaskAction.addBeforeActionPredicate(predicate);
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public ProcAction.BeforeActionPredicate getBeforeCancelProcessPredicate() {
        return beforeCancelProcessPredicate;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setBeforeCancelProcessPredicate(ProcAction.BeforeActionPredicate predicate) {
        this.beforeCancelProcessPredicate = predicate;
        if (cancelProcessAction != null)
            cancelProcessAction.addBeforeActionPredicate(predicate);
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public boolean isStartProcessEnabled() {
        return startProcessEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setStartProcessEnabled(boolean startProcessEnabled) {
        this.startProcessEnabled = startProcessEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public boolean isCancelProcessEnabled() {
        return cancelProcessEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setCancelProcessEnabled(boolean cancelProcessEnabled) {
        this.cancelProcessEnabled = cancelProcessEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public boolean isCompleteTaskEnabled() {
        return completeTaskEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setCompleteTaskEnabled(boolean completeTaskEnabled) {
        this.completeTaskEnabled = completeTaskEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public boolean isClaimTaskEnabled() {
        return claimTaskEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setClaimTaskEnabled(boolean claimTaskEnabled) {
        this.claimTaskEnabled = claimTaskEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public boolean isTaskInfoEnabled() {
        return taskInfoEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setTaskInfoEnabled(boolean taskInfoEnabled) {
        this.taskInfoEnabled = taskInfoEnabled;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public String getButtonWidth() {
        return buttonWidth;
    }

    /**
     * @deprecated Initialize the frame with the {@link Initializer} that can be obtained by the {@link #initializer()}
     * method.
     */
    @Deprecated
    public void setButtonWidth(String buttonWidth) {
        this.buttonWidth = buttonWidth;
    }
}
