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
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class StartProcessAction extends ProcAction {

    protected ProcInstance procInstance;
    protected final ProcessRuntimeService processRuntimeService;
    protected final ProcessFormService processFormService;
    protected Component.BelongToFrame target;
    protected DataManager dataManager;

    public StartProcessAction(ProcInstance procInstance, Component.BelongToFrame target) {
        super("startProcess");
        this.procInstance = procInstance;
        this.target = target;
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);
        processFormService = AppBeans.get(ProcessFormService.class);
        dataManager = AppBeans.get(DataManager.class);
    }

    @Override
    public void actionPerform(Component component) {
        if (!evaluateBeforeActionPredicates()) return;

        reloadOrPersistProcInstance();

        ProcFormDefinition startForm = processFormService.getStartForm(procInstance.getProcDefinition());
        if (startForm != null) {
            Map<String, Object> formParams = new HashMap<>();
            formParams.put("procInstance", procInstance);
            formParams.put("formDefinition", startForm);
            final Window window = target.getFrame().openWindow(startForm.getName(), WindowManager.OpenType.DIALOG, formParams);
            window.addCloseListener(actionId -> {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    String comment = null;
                    Map<String, Object> formResult = null;
                    if (window instanceof ProcForm) {
                        comment = ((ProcForm) window).getComment();
                        formResult = ((ProcForm) window).getFormResult();
                    }
                    _startProcess(comment, formResult);
                }
            });
        } else {
            _startProcess(null, new HashMap<>());
        }
    }

    /**
     * There may be a case when the passed procInstance object seems to be a new instance,
     * but in fact it was persisted after it was passed to the action.
     * The method checks this case and loads the procInstance from the database if necessary.
     * If the procInstance was not persisted, it is persisted in this method.
     */
    protected void reloadOrPersistProcInstance() {
        if (PersistenceHelper.isNew(procInstance)) {
            LoadContext<ProcInstance> ctx = new LoadContext<>(ProcInstance.class)
                    .setId(procInstance.getId())
                    .setView("procInstance-start");
            ProcInstance reloadedProcInstance = dataManager.load(ctx);
            if (reloadedProcInstance == null) {
                Set<Entity> commitedEntities = dataManager.commit(new CommitContext(procInstance));
                reloadedProcInstance = (ProcInstance) commitedEntities.iterator().next();
            }
            procInstance = reloadedProcInstance;
        }
    }

    protected void _startProcess(String startComment, Map<String, Object> processVariables) {
        processRuntimeService.startProcess(procInstance,startComment, processVariables);
        fireAfterActionListeners();
    }

    @Override
    public String getCaption() {
        return messages.getMessage(StartProcessAction.class, "startProcess");
    }
}
