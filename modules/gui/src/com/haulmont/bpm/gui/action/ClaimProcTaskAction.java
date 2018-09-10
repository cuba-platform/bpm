/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.security.global.UserSession;

public class ClaimProcTaskAction extends ProcAction {

    protected final ProcessRuntimeService processRuntimeService;
    protected ProcTask procTask;

    public ClaimProcTaskAction(ProcTask procTask) {
        super("claimProcTask");
        this.procTask = procTask;
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);

        Messages messages = AppBeans.get(Messages.NAME);
        this.caption = messages.getMessage(getClass(), "claimTask");
    }

    @Override
    public void actionPerform(Component component) {
        if (!evaluateBeforeActionPredicates()) {
            return;
        }

        Messages messages = AppBeans.get(Messages.NAME);

        String claimTaskDialogTitle = messages.getMessage(ClaimProcTaskAction.class, "claimTaskDialogTitle");
        String claimTaskDialogMsg = messages.getMessage(ClaimProcTaskAction.class, "claimTaskDialogMsg");
        ActionOwner owner = getOwner();
        if (owner instanceof Component.BelongToFrame) {
            Frame frame = ((Component.BelongToFrame) owner).getFrame();
            LegacyFrame.of(frame)
                    .showOptionDialog(claimTaskDialogTitle, claimTaskDialogMsg, Frame.MessageType.CONFIRMATION, new Action[]{
                            new DialogAction(Type.YES) {
                                @Override
                                public void actionPerform(Component component) {
                                    UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.NAME);

                                    UserSession userSession = userSessionSource.getUserSession();
                                    processRuntimeService.claimProcTask(procTask, userSession.getCurrentOrSubstitutedUser());
                                    fireAfterActionListeners();
                                }
                            },
                            new DialogAction(Type.NO, Status.PRIMARY)
                    });
        }
    }
}