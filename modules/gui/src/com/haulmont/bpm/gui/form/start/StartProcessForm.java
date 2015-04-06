/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */
package com.haulmont.bpm.gui.form.start;

import com.haulmont.bpm.gui.form.AbstractProcForm;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.TextArea;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gorbunkov
 */
public class StartProcessForm extends AbstractProcForm {

    @Inject
    protected TextArea comment;

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
        close(COMMIT_ACTION_ID);
    }

    public void onWindowClose() {
        close(CLOSE_ACTION_ID);
    }
}