/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.IFrame;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ClaimProcTaskAction extends ProcAction {

    protected ProcTask procTask;
    private Component.BelongToFrame target;
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
        target.getFrame().showOptionDialog(claimTaskDialogTitle, claimTaskDialogMsg, IFrame.MessageType.CONFIRMATION, new Action[] {
                new DialogAction(DialogAction.Type.YES) {
                    @Override
                    public void actionPerform(Component component) {
                        processRuntimeService.claimProcTask(procTask, userSession.getCurrentOrSubstitutedUser());
                        fireAfterActionListeners();
                    }
                },
                new DialogAction(DialogAction.Type.NO) {}
        });
    }

    @Override
    public String getCaption() {
        return messages.getMessage(getClass(), "claimTask");
    }
}
