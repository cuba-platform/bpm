/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.gui.form;

import com.haulmont.cuba.gui.components.AbstractWindow;

import java.util.HashMap;
import java.util.Map;

/**
 * Empty implementation of {@link ProcForm}. Use it if you need to implement only few methods.
 * @author gorbunkov
 * @version $Id$
 */
public abstract class AbstractProcForm extends AbstractWindow implements ProcForm {

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public Map<String, Object> getFormResult() {
        return new HashMap<>();
    }

}
