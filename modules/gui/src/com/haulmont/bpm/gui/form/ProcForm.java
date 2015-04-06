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
public interface ProcForm {

     String getComment();

    public Map<String, Object> getProcessVariables();

}
