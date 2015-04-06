/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.gui.form;

import com.haulmont.cuba.gui.components.AbstractWindow;

import java.util.HashMap;
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
    public Map<String, Object> getProcessVariables() {
        return new HashMap<>();
    }
}
