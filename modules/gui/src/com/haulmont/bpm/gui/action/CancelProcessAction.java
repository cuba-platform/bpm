/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class CancelProcessAction extends ProcAction {

    private ProcInstance procInstance;
    private Component.BelongToFrame target;
    protected final ProcessRuntimeService processRuntimeService;
    protected final ProcessFormService processFormService;

    public CancelProcessAction(ProcInstance procInstance, Component.BelongToFrame target) {
        super("cancelProcess");
        this.procInstance = procInstance;
        this.target = target;
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);
        processFormService = AppBeans.get(ProcessFormService.class);
    }

    @Override
    public void actionPerform(Component component) {
        if (!evaluateBeforeActionPredicates()) return;
        ProcFormDefinition cancelForm = processFormService.getCancelForm(procInstance.getProcDefinition());
        Map<String, Object> params = new HashMap<>();
        params.put("formDefinition", cancelForm);
        final Window window = target.getFrame().openWindow(cancelForm.getName(), WindowManager.OpenType.DIALOG, params);
        window.addListener(new Window.CloseListener() {
            @Override
            public void windowClosed(String actionId) {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    String comment = null;
                    if (window instanceof ProcForm) {
                        comment = ((ProcForm) window).getComment();
                    }
                    processRuntimeService.cancelProcess(procInstance, comment);
                    fireAfterActionListeners();
                }
            }
        });
    }

    @Override
    public String getCaption() {
        return messages.getMessage(CancelProcessAction.class, "cancelProcess");
    }
}
