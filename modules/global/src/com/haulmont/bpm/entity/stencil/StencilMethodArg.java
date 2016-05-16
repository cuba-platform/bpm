/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.entity.stencil;

import com.haulmont.bali.util.ReflectionHelper;
import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;

import javax.persistence.ManyToOne;

@MetaClass(name = "bpm$StencilMethodArg")
public class StencilMethodArg extends AbstractNotPersistentEntity {

    @MetaProperty
    protected String propertyPackageTitle;

    @MetaProperty
    protected String type;

    @MetaProperty
    protected String defaultValue;

    @MetaProperty
    @ManyToOne
    protected ServiceTaskStencil stencil;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @MetaProperty
    public String getSimpleTypeName() {
        return type.substring(type.lastIndexOf(".") + 1);
    }

    public String getPropertyPackageType() {
        Class clazz;
        try {
            clazz = ReflectionHelper.loadClass(type);
        } catch (ClassNotFoundException e) {
            return "String";
        }

        if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz))
            return "Boolean";

        return "String";
    }

}
