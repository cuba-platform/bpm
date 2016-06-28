/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;

import java.util.HashMap;
import java.util.Map;

public class CompleteProcTaskAction extends ProcAction {

    protected final ProcessRuntimeService processRuntimeService;
    protected ProcTask procTask;
    protected String outcome;
    protected ProcFormDefinition formDefinition;
    protected Component.BelongToFrame target;
    protected final ProcessMessagesService processMessagesService;

    public CompleteProcTaskAction(ProcTask procTask, String outcome, ProcFormDefinition formDefinition, Component.BelongToFrame target) {
        super("completeTask_" + outcome);
        DataManager dataManager = AppBeans.get(DataManager.class);
        if (procTaskReloadRequired()) {
            this.procTask = dataManager.reload(procTask, "procTask-complete");
        } else {
            this.procTask = procTask;
        }
        this.outcome = outcome;
        this.formDefinition = formDefinition;
        this.target = target;
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);
        processMessagesService = AppBeans.get(ProcessMessagesService.class);
    }

    private boolean procTaskReloadRequired() {
        //todo gorbunkov
        return true;
    }

    @Override
    public void actionPerform(Component component) {
        if (!evaluateBeforeActionPredicates()) return;
        if (formDefinition != null) {
            Map<String, Object> formParams = new HashMap<>();
            formParams.put("formDefinition", formDefinition);
            formParams.put("procTask", procTask);
            formParams.put("procInstance", procTask.getProcInstance());

            final Window procForm = target.getFrame().openWindow(formDefinition.getName(), WindowManager.OpenType.DIALOG, formParams);
            procForm.addCloseListener(actionId -> {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    String comment = null;
                    Map<String, Object> formResult = null;
                    if (procForm instanceof ProcForm) {
                        comment = ((ProcForm) procForm).getComment();
                        formResult = ((ProcForm) procForm).getFormResult();
                    }
                    processRuntimeService.completeProcTask(procTask, outcome, comment, formResult);
                    fireAfterActionListeners();
                }
            });
        } else {
            processRuntimeService.completeProcTask(procTask, outcome, null, new HashMap<>());
            fireAfterActionListeners();
        }
    }

    @Override
    public String getCaption() {
        String key = procTask.getActTaskDefinitionKey() + "." + outcome;
        String message = processMessagesService.getMessage(procTask.getProcInstance().getProcDefinition().getActId(), key);
        if (message.equals(key)) {
            message = outcome;
        }
        return message;
    }
}