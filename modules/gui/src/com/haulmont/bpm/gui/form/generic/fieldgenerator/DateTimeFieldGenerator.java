/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.form.generic.fieldgenerator;

import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.chile.core.datatypes.impl.DateTimeDatatype;
import com.haulmont.cuba.gui.components.DateField;
import com.haulmont.cuba.gui.components.Field;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class DateTimeFieldGenerator extends AbstractFormFieldGenerator {
    @Override
    public Field createField(ProcFormParam formParam, String actExecutionId) {
        DateField dateField = componentsFactory.createComponent(DateField.class);
        standardFieldInit(dateField, formParam);
        setFieldValue(dateField, formParam, DateTimeDatatype.NAME, actExecutionId);
        return dateField;
    }
}
