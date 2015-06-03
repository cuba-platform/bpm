/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.gui.proctaskactions;

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
public class ProcTaskActionsFrame extends AbstractFrame {

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
    protected VBoxLayout actionsBox;

    protected ProcTask procTask;
    protected ProcInstance procInstance;

    protected ProcAction.BeforeActionPredicate beforeStartProcessPredicate;
    protected ProcAction.BeforeActionPredicate beforeCompleteTaskPredicate;
    protected ProcAction.BeforeActionPredicate beforeClaimTaskPredicate;
    protected ProcAction.BeforeActionPredicate beforeCancelProcessPredicate;
    protected ProcAction.AfterActionListener afterStartProcessListener;
    protected ProcAction.AfterActionListener afterCompleteTaskListener;
    protected ProcAction.AfterActionListener afterClaimTaskListener;
    protected ProcAction.AfterActionListener afterCancelProcessListener;

    protected static final String BUTTON_WIDTH = "150px";

    public void init(ProcInstance procInstance) {
        actionsBox.removeAll();
        this.procInstance = procInstance;
        procTask = findProcTask();
        if (procTask == null) {
            if (startProcessAllowed())
                initStartProcessUI();
            else {
                noActionsAvailableLbl.setVisible(true);
                taskInfoGrid.setVisible(false);
            }
        } else if (procTask.getProcActor() != null
                && userSession.getCurrentOrSubstitutedUser().equals(procTask.getProcActor().getUser())) {
            initCompleteTaskUI();
        } else {
            initClaimTaskUI();
        }
        initAdditionalActions();
    }

    protected void initClaimTaskUI() {
        initTaskInfoGrid();
        Button claimTaskBtn = componentsFactory.createComponent(Button.class);
        claimTaskBtn.setWidth(BUTTON_WIDTH);;
        ClaimProcTaskAction claimProcTaskAction = new ClaimProcTaskAction(procTask, claimTaskBtn);
        claimProcTaskAction.addBeforeActionPredicate(beforeClaimTaskPredicate);
        claimProcTaskAction.addAfterActionListener(afterClaimTaskListener);
        claimTaskBtn.setAction(claimProcTaskAction);
        actionsBox.add(claimTaskBtn);
    }

    protected void initCompleteTaskUI() {
        initTaskInfoGrid();
        Map<String, ProcFormDefinition> outcomesWithForms = processFormService.getOutcomesWithForms(procTask);
        for (Map.Entry<String, ProcFormDefinition> entry : outcomesWithForms.entrySet()) {
            Button actionBtn = componentsFactory.createComponent(Button.class);
            actionBtn.setWidth(BUTTON_WIDTH);
            CompleteProcTaskAction action = new CompleteProcTaskAction(procTask, entry.getKey(), entry.getValue(), actionBtn);
            action.addBeforeActionPredicate(beforeCompleteTaskPredicate);
            action.addAfterActionListener(afterCompleteTaskListener);
            actionBtn.setAction(action);
            actionsBox.add(actionBtn);
        }
    }

    protected void initStartProcessUI() {
        taskInfoGrid.setVisible(false);
        Button startProcessBtn = componentsFactory.createComponent(Button.class);
        startProcessBtn.setWidth(BUTTON_WIDTH);
        StartProcessAction startProcessAction = new StartProcessAction(procInstance, startProcessBtn);
        startProcessAction.addBeforeActionPredicate(beforeStartProcessPredicate);
        startProcessAction.addAfterActionListener(afterStartProcessListener);
        startProcessBtn.setAction(startProcessAction);
        actionsBox.add(startProcessBtn);
    }

    protected void initTaskInfoGrid() {
        taskName.setValue(procTask.getLocName());
        taskStartDate.setValue(procTask.getStartDate());
    }


    protected void initAdditionalActions() {
        if (cancelProcessAllowed()) {
            Button cancelBtn = componentsFactory.createComponent(Button.class);
            cancelBtn.setWidth(BUTTON_WIDTH);;
            CancelProcessAction cancelProcessAction = new CancelProcessAction(procInstance, cancelBtn);
            cancelProcessAction.addBeforeActionPredicate(beforeCancelProcessPredicate);
            cancelProcessAction.addAfterActionListener(afterCancelProcessListener);
            cancelBtn.setAction(cancelProcessAction);
            actionsBox.add(cancelBtn);
        }
    }

    protected boolean cancelProcessAllowed() {
        return procInstance.getStartDate() != null
                && procInstance.getEndDate() == null
                && userSession.getCurrentOrSubstitutedUser().equals(procInstance.getStartedBy());
    }


    protected boolean startProcessAllowed() {
        return procInstance.getStartDate() == null;
    }

    protected ProcTask findProcTask() {
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
}
