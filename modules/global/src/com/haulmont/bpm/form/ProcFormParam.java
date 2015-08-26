/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.form;


import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.cuba.core.global.AppBeans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * POJO representation of form parameter from BPMN process xml
 * @author gorbunkov
 * @version $Id$
 */
public class ProcFormParam implements Serializable {

    protected String name;
    protected String caption;
    protected String typeName;
    protected String value;
    protected boolean editable;
    protected boolean required;
    protected String entityName;
    protected String entityLookupScreen;
    protected Map<String, Object> enumItems = new HashMap<>();
    protected ProcFormDefinition formDefinition;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityLookupScreen() {
        return entityLookupScreen;
    }

    public void setEntityLookupScreen(String entityLookupScreen) {
        this.entityLookupScreen = entityLookupScreen;
    }

    public Map<String, Object> getEnumItems() {
        return enumItems;
    }

    public void setEnumItems(Map<String, Object> enumItems) {
        this.enumItems = enumItems;
    }

    public ProcFormDefinition getFormDefinition() {
        return formDefinition;
    }

    public void setFormDefinition(ProcFormDefinition formDefinition) {
        this.formDefinition = formDefinition;
    }

    public String getLocCaption() {
        if (formDefinition != null && caption != null) {
            ProcessMessagesService processMessagesService = AppBeans.get(ProcessMessagesService.class);
            return processMessagesService.loadString(formDefinition.getActProcessDefinitionId(), caption);
        }
        if (caption == null) return name;
        return caption;
    }
}
