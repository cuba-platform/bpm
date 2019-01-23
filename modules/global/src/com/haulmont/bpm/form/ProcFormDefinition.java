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

package com.haulmont.bpm.form;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

/**
 * POJO representation of form definition from BPMN process xml
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
