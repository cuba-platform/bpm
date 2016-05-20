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
import java.util.UUID;

/**
 * <p>This frame is used for displaying process actions available for the current user.
 * Depending of the process instance associated with the frame, the frame may display buttons
 * that start the process, complete process actions or claim the process task.</p>
 *
 * <p>There are two ways to init the frame:
 * <ul>
 *     <li>With the {@link ProcInstance} object</li>
 *     <li>With the code of {@link ProcDefinition} and entity identifier</li>
 * </ul>
 * See the {@link #init(String, Entity)} and {@link #init(ProcInstance)} methods for details</p>
 */
public class ProcActionsFrame extends AbstractFrame {

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

    protected Logger log = LoggerFactory.getLogger(getClass());

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
    protected boolean cancelProcessEnabled = true;
    protected boolean completeTaskEnabled = true;
    protected boolean claimTaskEnabled = true;
    protected boolean taskInfoEnabled = true;

    protected StartProcessAction startProcessAction;
    protected CancelProcessAction cancelProcessAction;
    protected ClaimProcTaskAction claimProcTaskAction;
    protected List<CompleteProcTaskAction> completeProcTaskActions = new ArrayList<>();

    /**
     * The method tries to find the process instance by the specified process code and the entity.
     * If the process instance is not found then a new instance is created.
     * Then the UI with available actions for the current user and the process instance is initialized .
     * @param procCode process definition code
     * @param entity an entity
     */
    public void init(String procCode, Entity entity) {
        ProcDefinition procDefinition = findProcDefinition(procCode);
        if (procDefinition == null) {
            log.debug("Process definition with code{} not found", procCode);
            return;
        }
        procInstance = findProcInstance(procDefinition, entity);
        if (procInstance == null) {
            procInstance = metadata.create(ProcInstance.class);
            procInstance.setProcDefinition(procDefinition);
            procInstance.setEntityId(entity.getUuid());
            procInstance.setEntityName(entity.getMetaClass().getName());
            getDsContext().addBeforeCommitListener(context -> context.getCommitInstances().add(procInstance));
        }

        init(procInstance);
    }

    /**
     * Method creates and displays buttons with the actions available for the current user with the
     * specified process instance
     * @param procInstance a process instance
     */
    public void init(ProcInstance procInstance) {
        this.procInstance = procInstance;
        resetUI();
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

    protected void resetUI() {
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
        Button cancelBtn = componentsFactory.createComponent(Button.class);
        cancelBtn.setWidth(buttonWidth);
        cancelProcessAction = new CancelProcessAction(procInstance, cancelBtn);
        cancelProcessAction.addBeforeActionPredicate(beforeCancelProcessPredicate);
        cancelProcessAction.addAfterActionListener(afterCancelProcessListener);
        cancelBtn.setAction(cancelProcessAction);
        actionsBox.add(cancelBtn);
    }

    protected void initTaskInfoGrid() {
        taskInfoGrid.setVisible(true);
        taskName.setValue(procTask.getLocName());
        taskStartDate.setValue(procTask.getStartDate());
    }

    protected boolean cancelProcessAllowed() {
        return procInstance.getStartDate() != null
                && procInstance.getEndDate() == null
                && userSession.getCurrentOrSubstitutedUser().equals(procInstance.getStartedBy());
    }

    protected boolean startProcessAllowed() {
        return procInstance.getStartDate() == null;
    }

    protected ProcTask findCurrentUserProcTask() {
        LoadContext ctx = new LoadContext(ProcTask.class);
        ctx.setQueryString("select pt from bpm$ProcTask pt left join pt.procActor pa left join pa.user pau " +
                "where pt.procInstance.id = :procInstance and (pau.id = :userId or " +
                "(pa is null and exists(select pt2 from bpm$ProcTask pt2 join pt2.candidateUsers cu where pt2.id = pt.id and cu.id = :userId))) " +
                "and pt.endDate is null")
                .setParameter("procInstance", procInstance)
                .setParameter("userId", userSession.getCurrentOrSubstitutedUser());
        ctx.setView("procTask-complete");
        return dataManager.<ProcTask>load(ctx);
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

    public ProcAction.AfterActionListener getAfterStartProcessListener() {
        return afterStartProcessListener;
    }

    public void setAfterStartProcessListener(ProcAction.AfterActionListener listener) {
        this.afterStartProcessListener = listener;
        if (startProcessAction != null) startProcessAction.addAfterActionListener(listener);
    }

    public ProcAction.AfterActionListener getAfterCompleteTaskListener() {
        return afterCompleteTaskListener;
    }

    public void setAfterCompleteTaskListener(ProcAction.AfterActionListener listener) {
        this.afterCompleteTaskListener = listener;
        completeProcTaskActions.stream().forEach(action ->
                action.addAfterActionListener(listener));
    }

    public ProcAction.BeforeActionPredicate getBeforeStartProcessPredicate() {
        return beforeStartProcessPredicate;
    }

    public void setBeforeStartProcessPredicate(ProcAction.BeforeActionPredicate predicate) {
        this.beforeStartProcessPredicate = predicate;
        if (startProcessAction != null)
            startProcessAction.addBeforeActionPredicate(predicate);
    }

    public ProcAction.AfterActionListener getAfterClaimTaskListener() {
        return afterClaimTaskListener;
    }

    public void setAfterClaimTaskListener(ProcAction.AfterActionListener listener) {
        this.afterClaimTaskListener = listener;
        if (claimProcTaskAction != null)
            claimProcTaskAction.addAfterActionListener(listener);
    }

    public ProcAction.AfterActionListener getAfterCancelProcessListener() {
        return afterCancelProcessListener;
    }

    public void setAfterCancelProcessListener(ProcAction.AfterActionListener listener) {
        this.afterCancelProcessListener = listener;
        if (cancelProcessAction != null)
            cancelProcessAction.addAfterActionListener(listener);
    }

    public ProcAction.BeforeActionPredicate getBeforeCompleteTaskPredicate() {
        return beforeCompleteTaskPredicate;
    }

    public void setBeforeCompleteTaskPredicate(ProcAction.BeforeActionPredicate predicate) {
        this.beforeCompleteTaskPredicate = predicate;
        completeProcTaskActions.stream().forEach(action -> action.addBeforeActionPredicate(predicate));
    }

    public ProcAction.BeforeActionPredicate getBeforeClaimTaskPredicate() {
        return beforeClaimTaskPredicate;
    }

    public void setBeforeClaimTaskPredicate(ProcAction.BeforeActionPredicate predicate) {
        this.beforeClaimTaskPredicate = predicate;
        if (claimProcTaskAction != null)
            claimProcTaskAction.addBeforeActionPredicate(predicate);
    }

    public ProcAction.BeforeActionPredicate getBeforeCancelProcessPredicate() {
        return beforeCancelProcessPredicate;
    }

    public void setBeforeCancelProcessPredicate(ProcAction.BeforeActionPredicate predicate) {
        this.beforeCancelProcessPredicate = predicate;
        if (cancelProcessAction != null)
            cancelProcessAction.addBeforeActionPredicate(predicate);
    }

    public boolean isStartProcessEnabled() {
        return startProcessEnabled;
    }

    public void setStartProcessEnabled(boolean startProcessEnabled) {
        this.startProcessEnabled = startProcessEnabled;
    }

    public boolean isCancelProcessEnabled() {
        return cancelProcessEnabled;
    }

    public void setCancelProcessEnabled(boolean cancelProcessEnabled) {
        this.cancelProcessEnabled = cancelProcessEnabled;
    }

    public boolean isCompleteTaskEnabled() {
        return completeTaskEnabled;
    }

    public void setCompleteTaskEnabled(boolean completeTaskEnabled) {
        this.completeTaskEnabled = completeTaskEnabled;
    }

    public boolean isClaimTaskEnabled() {
        return claimTaskEnabled;
    }

    public void setClaimTaskEnabled(boolean claimTaskEnabled) {
        this.claimTaskEnabled = claimTaskEnabled;
    }

    public boolean isTaskInfoEnabled() {
        return taskInfoEnabled;
    }

    public void setTaskInfoEnabled(boolean taskInfoEnabled) {
        this.taskInfoEnabled = taskInfoEnabled;
    }

    public String getButtonWidth() {
        return buttonWidth;
    }

    public void setButtonWidth(String buttonWidth) {
        this.buttonWidth = buttonWidth;
    }
}
