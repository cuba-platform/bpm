/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.entity.stencil;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;

import javax.persistence.ManyToOne;

@MetaClass(name = "bpm$ServiceTaskStencilMethodArg")
public class ServiceTaskStencilMethodArg extends AbstractNotPersistentEntity {

    @MetaProperty
    protected String propertyPackageName;

    @MetaProperty
    protected String propertyPackageTitle;

    @MetaProperty
    protected ServiceTaskStencilMethodArgType type;

    @MetaProperty
    protected Boolean visible = true;

    @MetaProperty
    protected String defaultValue;

    @MetaProperty
    @ManyToOne
    protected ServiceTaskStencil stencil;

    public String getPropertyPackageName() {
        return propertyPackageName;
    }

    public void setPropertyPackageName(String propertyPackageName) {
        this.propertyPackageName = propertyPackageName;
    }

    public ServiceTaskStencilMethodArgType getType() {
        return type;
    }

    public void setType(ServiceTaskStencilMethodArgType type) {
        this.type = type;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ServiceTaskStencil getStencil() {
        return stencil;
    }

    public void setStencil(ServiceTaskStencil stencil) {
        this.stencil = stencil;
    }

    public String getPropertyPackageTitle() {
        return propertyPackageTitle;
    }

    public void setPropertyPackageTitle(String propertyPackageTitle) {
        this.propertyPackageTitle = propertyPackageTitle;
    }

}
