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
import com.haulmont.bpm.BpmConstants;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.PersistenceHelper;
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

public class CompleteProcTaskAction extends ProcAction {

    private static final Logger log = LoggerFactory.getLogger(CompleteProcTaskAction.class);

    protected final ProcessRuntimeService processRuntimeService;
    protected ProcTask procTask;
    protected String outcome;
    protected ProcFormDefinition formDefinition;
    protected final ProcessMessagesService processMessagesService;

    public CompleteProcTaskAction(ProcTask procTask, String outcome, ProcFormDefinition formDefinition) {
        super("completeTask_" + outcome);
        DataManager dataManager = AppBeans.get(DataManager.class);
        this.procTask = PersistenceHelper.isLoadedWithView(procTask, BpmConstants.Views.PROC_TASK_COMPLETE) ?
                procTask :
                dataManager.reload(procTask, BpmConstants.Views.PROC_TASK_COMPLETE);
        this.outcome = outcome;
        this.formDefinition = formDefinition;
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);
        processMessagesService = AppBeans.get(ProcessMessagesService.class);
    }

    @Override
    public void actionPerform(Component component) {
        if (!evaluateBeforeActionPredicates()) return;

        Map<String, Object> variablesFromSupplier = processVariablesSupplier != null ?
                processVariablesSupplier.get() :
                new HashMap<>();

        if (formDefinition != null && !Strings.isNullOrEmpty(formDefinition.getName())) {
            Map<String, Object> formParams = new HashMap<>();
            formParams.put("formDefinition", formDefinition);
            formParams.put("procTask", procTask);
            formParams.put("procInstance", procTask.getProcInstance());
            formParams.put("caption", CompleteProcTaskAction.this.getCaption());
            formParams.put("outcome", outcome);

            if (screenParametersSupplier != null) {
                Map<String, Object> screenParameters = screenParametersSupplier.get();
                if (screenParameters != null) {
                    formParams.putAll(screenParameters);
                }
            }

            ActionOwner owner = getOwner();
            if (owner instanceof Component.BelongToFrame) {
                WindowManager wm = (WindowManager) getScreenContext((Component.BelongToFrame) owner).getScreens();
                WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo(formDefinition.getName());

                Window procForm = wm.openWindow(windowInfo, OpenType.DIALOG, formParams);
                procForm.addCloseListener(actionId -> {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        String comment = null;
                        Map<String, Object> formProcessVariables = new HashMap<>();
                        if (procForm instanceof ProcForm) {
                            comment = ((ProcForm) procForm).getComment();
                            formProcessVariables = ((ProcForm) procForm).getFormResult();
                        }
                        formProcessVariables.putAll(variablesFromSupplier);
                        processRuntimeService.completeProcTask(procTask, outcome, comment, formProcessVariables);
                        fireAfterActionListeners();
                    }
                });
            } else {
                log.error("Action owner must implement Component.BelongToFrame");
            }
        } else {
            processRuntimeService.completeProcTask(procTask, outcome, null, variablesFromSupplier);
            fireAfterActionListeners();
        }
    }

    @Override
    public String getCaption() {
        if (!Strings.isNullOrEmpty(this.caption)) return this.caption;
        String key = procTask.getActTaskDefinitionKey() + "." + outcome;
        String message = processMessagesService.getMessage(procTask.getProcInstance().getProcDefinition().getActId(), key);
        if (message.equals(key)) {
            message = outcome;
        }
        return message;
    }

    public ProcTask getProcTask() {
        return procTask;
    }

    public void setProcTask(ProcTask procTask) {
        this.procTask = procTask;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public ProcFormDefinition getFormDefinition() {
        return formDefinition;
    }

    public void setFormDefinition(ProcFormDefinition formDefinition) {
        this.formDefinition = formDefinition;
    }
}