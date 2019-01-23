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

package com.haulmont.bpm.entity.stencil;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.BaseUuidEntity;

import javax.persistence.ManyToOne;

@NamePattern(value = "%s|title")
@MetaClass(name = "bpm$Stencil")
public class Stencil extends BaseUuidEntity {

    private static final long serialVersionUID = -4472387482567220647L;

    @MetaProperty(mandatory = true)
    protected String stencilId;

    @MetaProperty(mandatory = true)
    protected String title;

    @MetaProperty
    protected String description;

    @MetaProperty
    protected Boolean editable = false;

    @MetaProperty
    protected Integer orderNo;

    @MetaProperty(mandatory = true)
    @ManyToOne
    protected GroupStencil parentGroup;

    public String getStencilId() {
        return stencilId;
    }

    public void setStencilId(String stencilId) {
        this.stencilId = stencilId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public GroupStencil getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(GroupStencil parentGroup) {
        this.parentGroup = parentGroup;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
