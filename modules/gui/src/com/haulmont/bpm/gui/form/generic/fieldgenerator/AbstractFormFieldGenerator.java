/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.form.generic.fieldgenerator;

import com.google.common.base.Strings;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import java.text.ParseException;
import java.util.regex.Pattern;

/**
 * Abstract class contains common behaviour to FormFieldGenerators
 */
public abstract class AbstractFormFieldGenerator implements FormFieldGenerator {

    protected static final String EXPRESSION_REGEX = "[$#]\\{.*\\}";
    protected static final Pattern expressionPattern = Pattern.compile(EXPRESSION_REGEX);

    protected ComponentsFactory componentsFactory;

    protected Messages messages;
    protected ProcessRuntimeService processRuntimeService;

    public AbstractFormFieldGenerator() {
        componentsFactory = AppBeans.get(ComponentsFactory.class);
        messages = AppBeans.get(Messages.class);
        processRuntimeService = AppBeans.get(ProcessRuntimeService.class);
    }

    /**
     * Sets the value from the {@code formParam} to the field. Before setting is performed the value is
     * parsed or if an UEL expression is stored in {@code formParam} this expression is evaluated.
     */
    protected void setFieldValue(Field field, ProcFormParam formParam, Datatype datatype, String actExecutionId) {
        if (!Strings.isNullOrEmpty(formParam.getValue())) {
            try {
                Object value;
                if (isExpression(formParam.getValue())) {
                    value = processRuntimeService.evaluateExpression(formParam.getValue(), actExecutionId);
                } else {
                    value = datatype.parse(formParam.getValue());
                }
                field.setValue(value);
            } catch (ParseException e) {
                throw new BpmException("Error when parsing process form parameter value", e);
            }
        }
    }

    protected void standardFieldInit(Field field, ProcFormParam formParam) {
        field.setRequired(formParam.isRequired());
        field.setEditable(formParam.isEditable());
        field.setRequiredMessage(messages.formatMessage(AbstractFormFieldGenerator.class, "fillField", formParam.getLocCaption()));
    }

    protected boolean isExpression(String value) {
        return expressionPattern.matcher(value).matches();
    }
}
