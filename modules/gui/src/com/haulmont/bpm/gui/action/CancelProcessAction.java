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

import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.ActionOwner;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CancelProcessAction extends ProcAction {

    private static final Logger log = LoggerFactory.getLogger(CancelProcessAction.class);

    protected ProcInstance procInstance;
    protected final ProcessRuntimeService processRuntimeService;
    protected final ProcessFormService processFormService;
    protected final ScreenBuilders screenBuilders;

    public CancelProcessAction(ProcInstance procInstance) {
        super("cancelProcess");
        this.procInstance = procInstance;
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);
        processFormService = AppBeans.get(ProcessFormService.class);
        screenBuilders = AppBeans.get(ScreenBuilders.class);

        Messages messages = AppBeans.get(Messages.NAME);
        this.caption = messages.getMessage(CancelProcessAction.class, "cancelProcess");
    }

    @Override
    public void actionPerform(Component component) {
        if (!evaluateBeforeActionPredicates()) {
            return;
        }

        ProcFormDefinition cancelForm = processFormService.getCancelForm(procInstance.getProcDefinition());
        ActionOwner owner = getOwner();
        if (owner instanceof Component.BelongToFrame) {
            Map<String, Object> params = new HashMap<>();
            params.put("formDefinition", cancelForm);
            Screen screen = screenBuilders.screen(((Component.BelongToFrame) owner).getFrame().getFrameOwner())
                    .withScreenId(cancelForm.getName())
                    .withOpenMode(OpenMode.DIALOG)
                    .withOptions(new MapScreenOptions(params))
                    .build();
            screen.addAfterCloseListener(afterCloseEvent -> {
                String comment = null;
                CloseAction closeAction = afterCloseEvent.getCloseAction();
                if (closeAction instanceof StandardCloseAction &&
                        Window.COMMIT_ACTION_ID.equals(((StandardCloseAction) closeAction).getActionId())) {
                    comment = ((ProcForm) screen).getComment();
                    processRuntimeService.cancelProcess(procInstance, comment);
                    fireAfterActionListeners();
                }
            });
            screen.show();
        } else {
            log.error("Action owner must implement Component.BelongToFrame");
        }
    }
}