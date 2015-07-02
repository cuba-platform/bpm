/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.form.generic.fieldgenerator;

import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.bpm.gui.form.generic.GenericProcForm;
import com.haulmont.cuba.gui.components.Field;

/**
 * Interface to be implemented by field generators of {@link GenericProcForm}
 * @author gorbunkov
 * @version $Id$
 */
public interface FormFieldGenerator {
    Field createField(ProcFormParam formParam, String actExecutionId);
}
