/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.procactions;

import com.haulmont.bpm.BpmConstants;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.action.*;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.global.UserSession;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
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
        actionsBox.removeAll();
        noActionsAvailableLbl.setVisible(true);
        taskInfoGrid.setVisible(false);
    }

    protected void initClaimTaskUI() {
        if (taskInfoEnabled)
            initTaskInfoGrid();
        noActionsAvailableLbl.setVisible(false);
        Button claimTaskBtn = componentsFactory.createComponent(Button.class);
        claimTaskBtn.setWidth(buttonWidth);;
        ClaimProcTaskAction claimProcTaskAction = new ClaimProcTaskAction(procTask, claimTaskBtn);
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
        }
    }

    protected void initStartProcessUI() {
        taskInfoGrid.setVisible(false);
        noActionsAvailableLbl.setVisible(false);
        Button startProcessBtn = componentsFactory.createComponent(Button.class);
        startProcessBtn.setWidth(buttonWidth);
        StartProcessAction startProcessAction = new StartProcessAction(procInstance, startProcessBtn);
        startProcessAction.addBeforeActionPredicate(beforeStartProcessPredicate);
        startProcessAction.addAfterActionListener(afterStartProcessListener);
        startProcessBtn.setAction(startProcessAction);
        actionsBox.add(startProcessBtn);
    }

    protected void initCancelAction() {
        Button cancelBtn = componentsFactory.createComponent(Button.class);
        cancelBtn.setWidth(buttonWidth);
        CancelProcessAction cancelProcessAction = new CancelProcessAction(procInstance, cancelBtn);
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
        ctx.setQueryString("select pt from bpm$ProcTask pt left join pt.candidateUsers cu " +
                "where pt.procInstance.id = :procInstance and (pt.procActor.user.id = :userId or (pt.procActor is null and cu.id = :userId)) " +
                "and pt.endDate is null")
                .setParameter("procInstance", procInstance)
                .setParameter("userId", userSession.getCurrentOrSubstitutedUser());
        ctx.setView("procTask-complete");
        return dataManager.load(ctx);
    }

    public ProcAction.AfterActionListener getAfterStartProcessListener() {
        return afterStartProcessListener;
    }

    public void setAfterStartProcessListener(ProcAction.AfterActionListener afterStartProcessListener) {
        this.afterStartProcessListener = afterStartProcessListener;
    }

    public ProcAction.AfterActionListener getAfterCompleteTaskListener() {
        return afterCompleteTaskListener;
    }

    public void setAfterCompleteTaskListener(ProcAction.AfterActionListener afterCompleteTaskListener) {
        this.afterCompleteTaskListener = afterCompleteTaskListener;
    }

    public ProcAction.BeforeActionPredicate getBeforeStartProcessPredicate() {
        return beforeStartProcessPredicate;
    }

    public void setBeforeStartProcessPredicate(ProcAction.BeforeActionPredicate beforeStartProcessPredicate) {
        this.beforeStartProcessPredicate = beforeStartProcessPredicate;
    }

    public ProcAction.AfterActionListener getAfterClaimTaskListener() {
        return afterClaimTaskListener;
    }

    public void setAfterClaimTaskListener(ProcAction.AfterActionListener afterClaimTaskListener) {
        this.afterClaimTaskListener = afterClaimTaskListener;
    }

    public ProcAction.AfterActionListener getAfterCancelProcessListener() {
        return afterCancelProcessListener;
    }

    public void setAfterCancelProcessListener(ProcAction.AfterActionListener afterCancelProcessListener) {
        this.afterCancelProcessListener = afterCancelProcessListener;
    }

    public ProcAction.BeforeActionPredicate getBeforeCompleteTaskPredicate() {
        return beforeCompleteTaskPredicate;
    }

    public void setBeforeCompleteTaskPredicate(ProcAction.BeforeActionPredicate beforeCompleteTaskPredicate) {
        this.beforeCompleteTaskPredicate = beforeCompleteTaskPredicate;
    }

    public ProcAction.BeforeActionPredicate getBeforeClaimTaskPredicate() {
        return beforeClaimTaskPredicate;
    }

    public void setBeforeClaimTaskPredicate(ProcAction.BeforeActionPredicate beforeClaimTaskPredicate) {
        this.beforeClaimTaskPredicate = beforeClaimTaskPredicate;
    }

    public ProcAction.BeforeActionPredicate getBeforeCancelProcessPredicate() {
        return beforeCancelProcessPredicate;
    }

    public void setBeforeCancelProcessPredicate(ProcAction.BeforeActionPredicate beforeCancelProcessPredicate) {
        this.beforeCancelProcessPredicate = beforeCancelProcessPredicate;
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
