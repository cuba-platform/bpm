/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.bpm.gui.form.generic.fieldgenerator;

import com.haulmont.bpm.gui.form.generic.GenericProcForm;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory that produces field generators for {@link GenericProcForm}
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
