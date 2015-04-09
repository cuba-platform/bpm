/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.gui.form;

import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.cuba.gui.components.AbstractWindow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class AbstractProcForm extends AbstractWindow implements ProcForm {

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public Map<String, Object> getFormResult() {
        return new HashMap<>();
    }

}
