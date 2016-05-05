/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.entity.stencil;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;

import javax.persistence.ManyToOne;

@NamePattern(value = "%s|title")
@MetaClass(name = "bpm$Stencil")
public class Stencil extends AbstractNotPersistentEntity {

    @MetaProperty(mandatory = true)
    protected String stencilId;

    @MetaProperty(mandatory = true)
    protected String title;

    @MetaProperty
    protected Boolean editable = false;

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
}
