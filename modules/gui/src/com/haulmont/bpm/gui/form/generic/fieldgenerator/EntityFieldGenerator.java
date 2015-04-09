/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
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

/**
 * @author gorbunkov
 * @version $Id$
 */
public class EntityFieldGenerator extends AbstractFormFieldGenerator {

    protected Metadata metadata;

    public EntityFieldGenerator() {
        super();
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
