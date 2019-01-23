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

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.ActionOwner;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.haulmont.cuba.gui.ComponentsHelper.getScreenContext;

public class StartProcessAction extends ProcAction {

    protected ProcInstance procInstance;
    protected final ProcessRuntimeService processRuntimeService;
    protected final ProcessFormService processFormService;
    protected DataManager dataManager;
    private static final Logger log = LoggerFactory.getLogger(StartProcessAction.class);

    public StartProcessAction(ProcInstance procInstance) {
        super("startProcess");
        this.procInstance = procInstance;
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);
        processFormService = AppBeans.get(ProcessFormService.class);
        dataManager = AppBeans.get(DataManager.class);
    }

    @Override
    public void actionPerform(Component component) {
        if (!evaluateBeforeActionPredicates()) return;

        ProcFormDefinition startForm = processFormService.getStartForm(procInstance.getProcDefinition());
        if (startForm != null) {
            Map<String, Object> formParams = new HashMap<>();
            formParams.put("procInstance", procInstance);
            formParams.put("formDefinition", startForm);
            formParams.put("caption", StartProcessAction.this.getCaption());
            formParams.put("isStartForm", true);
            if (screenParametersSupplier != null) {
                Map<String, Object> screenParameters = screenParametersSupplier.get();
                if (screenParameters != null) {
                    formParams.putAll(screenParameters);
                }
            }
            ActionOwner owner = getOwner();
            if (owner instanceof Component.BelongToFrame) {
                WindowManager wm = (WindowManager) getScreenContext((Component.BelongToFrame) owner).getScreens();
                WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo(startForm.getName());

                Window window = wm.openWindow(windowInfo, OpenType.DIALOG, formParams);
                window.addCloseListener(actionId -> {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        String comment = null;
                        Map<String, Object> formProcessVariables = null;
                        if (window instanceof ProcForm) {
                            comment = ((ProcForm) window).getComment();
                            formProcessVariables = ((ProcForm) window).getFormResult();
                        }
                        _startProcess(comment, formProcessVariables);
                    }
                });
            } else {
                log.error("Action owner must implement Component.BelongToFrame");
            }
        } else {
            _startProcess(null, new HashMap<>());
        }
    }

    protected void _startProcess(String startComment, Map<String, Object> processVariables) {
        if (processVariablesSupplier != null) {
            Map<String, Object> variablesFromSupplier = processVariablesSupplier.get();
            if (variablesFromSupplier != null) {
                processVariables.putAll(variablesFromSupplier);
            }
        }
        processRuntimeService.startProcess(procInstance, startComment, processVariables);
        fireAfterActionListeners();
    }

    @Override
    public String getCaption() {
        if (!Strings.isNullOrEmpty(this.caption)) {
            return this.caption;
        }
        Messages messages = AppBeans.get(Messages.NAME);
        return messages.getMessage(StartProcessAction.class, "startProcess");
    }
}