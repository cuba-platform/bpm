/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.gui.form;

import com.haulmont.bpm.form.ProcFormParam;

import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public interface ProcForm {

    String getComment();

    Map<String, Object> getFormResult();
}
