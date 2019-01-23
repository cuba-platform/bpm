/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.components.ActionOwner;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.cuba.security.global.UserSession;

import static com.haulmont.cuba.gui.ComponentsHelper.getScreenContext;

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
            Dialogs dialogs = getScreenContext((Component.BelongToFrame) owner).getDialogs();

            dialogs.createOptionDialog()
                    .withCaption(claimTaskDialogTitle)
                    .withMessage(claimTaskDialogMsg)
                    .withActions(
                            new DialogAction(Type.YES)
                                    .withHandler(event -> {
                                        UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.NAME);

                                        UserSession userSession = userSessionSource.getUserSession();
                                        processRuntimeService.claimProcTask(procTask, userSession.getCurrentOrSubstitutedUser());
                                        fireAfterActionListeners();
                                    }),
                            new DialogAction(Type.NO, Status.PRIMARY)
                    )
                    .show();
        }
    }
}