/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.form.generic.fieldgenerator;

import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.gui.components.TextField;

public class IntegerFieldGenerator extends AbstractFormFieldGenerator {

    @Override
    public TextField createField(ProcFormParam formParam, String actExecutionId) {
        TextField textField = componentsFactory.createComponent(TextField.class);
        Datatype datatype = Datatypes.getNN(Integer.class);
        textField.setDatatype(datatype);
        standardFieldInit(textField, formParam);
        setFieldValue(textField, formParam, datatype, actExecutionId);
        return textField;
    }
}