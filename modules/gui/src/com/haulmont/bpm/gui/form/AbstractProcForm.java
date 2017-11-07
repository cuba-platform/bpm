/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.form;

import com.haulmont.cuba.gui.components.AbstractWindow;

import java.util.HashMap;
import java.util.Map;

/**
 * Empty implementation of {@link ProcForm}. Use it if you need to implement only few methods.
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
