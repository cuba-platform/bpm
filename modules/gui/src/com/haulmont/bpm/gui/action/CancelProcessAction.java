/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.ActionOwner;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CancelProcessAction extends ProcAction {

    protected ProcInstance procInstance;
    protected final ProcessRuntimeService processRuntimeService;
    protected final ProcessFormService processFormService;
    private static final Logger log = LoggerFactory.getLogger(CancelProcessAction.class);

    public CancelProcessAction(ProcInstance procInstance) {
        super("cancelProcess");
        this.procInstance = procInstance;
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);
        processFormService = AppBeans.get(ProcessFormService.class);
    }

    @Override
    public void actionPerform(Component component) {
        if (!evaluateBeforeActionPredicates()) return;
        ProcFormDefinition cancelForm = processFormService.getCancelForm(procInstance.getProcDefinition());
        Map<String, Object> params = new HashMap<>();
        params.put("formDefinition", cancelForm);
        ActionOwner owner = getOwner();
        if (owner instanceof Component.BelongToFrame) {
            final Window window = ((Component.BelongToFrame) owner).getFrame().openWindow(cancelForm.getName(), WindowManager.OpenType.DIALOG, params);
            window.addCloseListener(actionId -> {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    String comment = null;
                    if (window instanceof ProcForm) {
                        comment = ((ProcForm) window).getComment();
                    }
                    processRuntimeService.cancelProcess(procInstance, comment);
                    fireAfterActionListeners();
                }
            });
        } else {
            log.error("Action owner must implement Component.BelongToFrame");
        }
    }

    @Override
    public String getCaption() {
        return messages.getMessage(CancelProcessAction.class, "cancelProcess");
    }
}