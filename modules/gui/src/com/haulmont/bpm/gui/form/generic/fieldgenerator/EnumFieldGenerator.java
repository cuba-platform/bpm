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

import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.LookupField;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnumFieldGenerator extends AbstractFormFieldGenerator {

    @Override
    public Field createField(ProcFormParam formParam, String actExecutionId) {
        LookupField lookupField = componentsFactory.createComponent(LookupField.class);

        Map<String, Object> localizedOptionsMap = getLocalizedOptionsMap(formParam);
        lookupField.setOptionsMap(localizedOptionsMap);

        lookupField.setValue(formParam.getValue());

        standardFieldInit(lookupField, formParam);
        return lookupField;
    }

    protected Map<String, Object> getLocalizedOptionsMap(ProcFormParam formParam) {
        ProcessMessagesService processMessagesService = AppBeans.get(ProcessMessagesService.class);
        String actProcessDefinitionId = formParam.getFormDefinition().getActProcessDefinitionId();
        Map<String, Object> enumItems = formParam.getEnumItems();
        Map<String, Object> localizedItems = new LinkedHashMap<>();
        for (String msgKey : enumItems.keySet()) {
            String locCaption = processMessagesService.loadString(actProcessDefinitionId, msgKey);
            localizedItems.put(locCaption, enumItems.get(msgKey));
        }
        return localizedItems;
    }
}