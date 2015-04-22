/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.gui.form.generic.fieldgenerator;

import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Field;

/**
 * Interface to be implemented by field generators of {@link com.haulmont.bpm.gui.form.generic.GenericProcessForm}
 * @author gorbunkov
 * @version $Id$
 */
public interface FormFieldGenerator {
    Field createField(ProcFormParam formParam, String actExecutionId);
}
