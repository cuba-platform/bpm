/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.action;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
            Component.ActionOwner owner = getOwner();
            if (owner instanceof Component.BelongToFrame) {
                final Window window = ((Component.BelongToFrame) owner).getFrame().openWindow(startForm.getName(), WindowManager.OpenType.DIALOG, formParams);
                window.addCloseListener(actionId -> {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        String comment = null;
                        Map<String, Object> formProcessVariables = null;
                        if (window instanceof ProcForm) {
                            comment = ((ProcForm) window).getComment();
                            formProcessVariables = ((ProcForm) window).getFormProcessVariables();
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
        if (!Strings.isNullOrEmpty(this.caption)) return this.caption;
        return messages.getMessage(StartProcessAction.class, "startProcess");
    }
}