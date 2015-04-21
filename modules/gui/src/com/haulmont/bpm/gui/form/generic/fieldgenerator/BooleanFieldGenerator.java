/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.gui.form.generic.fieldgenerator;

import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.chile.core.datatypes.impl.BooleanDatatype;
import com.haulmont.cuba.gui.components.CheckBox;
import com.haulmont.cuba.gui.components.Field;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class BooleanFieldGenerator extends AbstractFormFieldGenerator {

    @Override
    public Field createField(ProcFormParam formParam, String actExecutionId) {
        CheckBox checkBox = componentsFactory.createComponent(CheckBox.class);
        standardFieldInit(checkBox, formParam);
        setFieldValue(checkBox, formParam, BooleanDatatype.NAME, actExecutionId);
        return checkBox;
    }
}