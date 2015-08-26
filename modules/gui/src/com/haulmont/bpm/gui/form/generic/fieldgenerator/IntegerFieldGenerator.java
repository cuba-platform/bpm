/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.form.generic.fieldgenerator;

import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.datatypes.impl.IntegerDatatype;
import com.haulmont.cuba.gui.components.TextField;

/**
 * @author gorbunkov
 * @version $Id$
 */

public class IntegerFieldGenerator extends AbstractFormFieldGenerator {

    @Override
    public TextField createField(ProcFormParam formParam, String actExecutionId) {
        TextField textField = componentsFactory.createComponent(TextField.class);
        textField.setDatatype(Datatypes.get(IntegerDatatype.NAME));
        standardFieldInit(textField, formParam);
        setFieldValue(textField, formParam, IntegerDatatype.NAME, actExecutionId);
        return textField;
    }
}
