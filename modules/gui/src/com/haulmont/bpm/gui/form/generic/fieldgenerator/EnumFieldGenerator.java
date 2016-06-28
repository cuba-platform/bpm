/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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