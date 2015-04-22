/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.form;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * POJO representation of form definition from BPMN process xml
 * @author gorbunkov
 * @version $Id$
 */
public class ProcFormDefinition implements Serializable {

    protected String name;

    protected String caption;

    protected Map<String, ProcFormParam> params = new LinkedHashMap<>();

    protected String actProcessDefinitionId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ProcFormParam> getParams() {
        return params;
    }

    public void setParams(Map<String, ProcFormParam> params) {
        this.params = params;
    }

    public String getActProcessDefinitionId() {
        return actProcessDefinitionId;
    }

    public void setActProcessDefinitionId(String actProcessDefinitionId) {
        this.actProcessDefinitionId = actProcessDefinitionId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
