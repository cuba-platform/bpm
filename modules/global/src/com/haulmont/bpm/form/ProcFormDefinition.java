/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.form;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

/**
 * POJO representation of form definition from BPMN process xml
 * @author gorbunkov
 * @version $Id$
 */
public class ProcFormDefinition implements Serializable {

    protected String name;

    protected String caption;

    protected List<ProcFormParam> params = new ArrayList<>();

    protected String actProcessDefinitionId;

    protected boolean isDefault;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProcFormParam> getParams() {
        return params;
    }

    public void setParams(List<ProcFormParam> params) {
        this.params = params;
    }

    @Nullable
    public ProcFormParam getParam(String name) {
        for (ProcFormParam param : params) {
            if (name.equals(param.getName())) return param;
        }
        return null;
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

    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
