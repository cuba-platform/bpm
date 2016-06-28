/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.cuba.gui.components.Frame;

public class ClaimProcTaskAction extends ProcAction {

    protected ProcTask procTask;
    protected Component.BelongToFrame target;
    protected final ProcessRuntimeService processRuntimeService;

    public ClaimProcTaskAction(ProcTask procTask, Component.BelongToFrame target) {
        super("claimProcTask");
        this.procTask = procTask;
        this.target = target;
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);
    }

    @Override
    public void actionPerform(Component component) {
        if (!evaluateBeforeActionPredicates()) return;
        String claimTaskDialogTitle = messages.getMessage(ClaimProcTaskAction.class, "claimTaskDialogTitle");
        String claimTaskDialogMsg = messages.getMessage(ClaimProcTaskAction.class, "claimTaskDialogMsg");
        target.getFrame().showOptionDialog(claimTaskDialogTitle, claimTaskDialogMsg, Frame.MessageType.CONFIRMATION, new Action[] {
                new DialogAction(Type.YES) {
                    @Override
                    public void actionPerform(Component component) {
                        processRuntimeService.claimProcTask(procTask, userSession.getCurrentOrSubstitutedUser());
                        fireAfterActionListeners();
                    }
                },
                new DialogAction(Type.NO, Status.PRIMARY)
        });
    }

    @Override
    public String getCaption() {
        return messages.getMessage(getClass(), "claimTask");
    }
}