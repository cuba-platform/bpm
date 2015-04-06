/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.gui.form.procactor;

import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.gui.form.AbstractProcForm;
import com.haulmont.bpm.gui.procactor.ProcActorsFrame;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.TextArea;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gorbunkov
 */
public class ProcActorsProcessForm extends AbstractProcForm {

    @Inject
    protected TextArea comment;

    @Inject
    protected ProcActorsFrame procActorsFrame;

    @WindowParam(required = true)
    protected ProcInstance procInstance;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        procActorsFrame.setProcInstance(procInstance);
        procActorsFrame.refresh();
    }

    @Override
    public String getComment() {
        return comment.getValue();
    }

    @Override
    public Map<String, Object> getProcessVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("startComment", comment.getValue());
        return variables;
    }

    public void onWindowCommit() {
        procActorsFrame.commit();
        close(COMMIT_ACTION_ID);
    }

    public void onWindowClose() {
        close(CLOSE_ACTION_ID, true);
    }
}