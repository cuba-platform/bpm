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

import com.google.common.base.Strings;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.PickerField;

public class EntityFieldGenerator extends AbstractFormFieldGenerator {

    protected Metadata metadata;

    public EntityFieldGenerator() {
        metadata = AppBeans.get(Metadata.class);
    }

    @Override
    public Field createField(ProcFormParam formParam, String actExecutionId) {
        PickerField pickerField = componentsFactory.createComponent(PickerField.class);
        MetaClass metaClass = metadata.getClass(formParam.getEntityName());
        if (metaClass == null) {
            throw new BpmException("MetaClass " + formParam.getEntityName() + " not found");
        }
        pickerField.setMetaClass(metaClass);

        PickerField.LookupAction lookupAction = pickerField.addLookupAction();
        if (!Strings.isNullOrEmpty(formParam.getEntityLookupScreen())) {
            lookupAction.setLookupScreen(formParam.getEntityLookupScreen());
        }
        pickerField.addClearAction();

        setFieldValue(pickerField, formParam, actExecutionId);
        standardFieldInit(pickerField, formParam);

        return pickerField;
    }

    protected void setFieldValue(Field field, ProcFormParam formParam, String actExecutionId) {
        if (!Strings.isNullOrEmpty(formParam.getValue())) {

            Object value = null;
            if (isExpression(formParam.getValue())) {
                value = processRuntimeService.evaluateExpression(formParam.getValue(), actExecutionId);
            } else {
                EntityLoadInfoBuilder entityLoadInfoBuilder = AppBeans.get(EntityLoadInfoBuilder.class);
                EntityLoadInfo entityLoadInfo = entityLoadInfoBuilder.parse(formParam.getValue());
                if (entityLoadInfo != null) {
                    value = loadEntityInstance(entityLoadInfo);
                }
            }
            field.setValue(value);
        }
    }

    protected Entity loadEntityInstance(EntityLoadInfo info) {
        Metadata metadata = AppBeans.get(Metadata.class);
        if (info.isNewEntity()) {
            return metadata.create(info.getMetaClass());
        }

        LoadContext ctx = new LoadContext(info.getMetaClass()).setId(info.getId());
        if (info.getViewName() != null)
            ctx.setView(info.getViewName());
        Entity entity;
        try {
            DataManager dataManager = AppBeans.get(DataManager.class);
            entity = dataManager.load(ctx);
        } catch (Exception e) {
            throw new BpmException("Unable to load item: " + info, e);
        }
        return entity;
    }
}