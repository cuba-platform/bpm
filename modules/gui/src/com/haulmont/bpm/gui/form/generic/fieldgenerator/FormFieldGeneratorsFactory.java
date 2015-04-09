/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.bpm.gui.form.generic.fieldgenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */

public class FormFieldGeneratorsFactory {

    protected Map<String, FormFieldGenerator> generatorsMap;

    public FormFieldGeneratorsFactory() {
        init();
    }

    public void init() {
        generatorsMap = new HashMap<>();
        generatorsMap.put("string", new StringFieldGenerator());
        generatorsMap.put("boolean", new BooleanFieldGenerator());
        generatorsMap.put("int", new IntegerFieldGenerator());
        generatorsMap.put("long", new LongFieldGenerator());
        generatorsMap.put("decimal", new BigDecimalFieldGenerator());
        generatorsMap.put("date", new DateFieldGenerator());
        generatorsMap.put("dateTime", new DateTimeFieldGenerator());
        generatorsMap.put("entity", new EntityFieldGenerator());
        generatorsMap.put("enum", new EnumFieldGenerator());
    }

    public FormFieldGenerator createFormFieldGenerator(String paramTypeName) {
        return generatorsMap.get(paramTypeName);
    }
}
