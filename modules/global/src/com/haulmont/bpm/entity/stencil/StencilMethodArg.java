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

import com.haulmont.bali.util.ReflectionHelper;
import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;

import javax.persistence.ManyToOne;

@MetaClass(name = "bpm$StencilMethodArg")
public class StencilMethodArg extends BaseUuidEntity {

    private static final long serialVersionUID = 6663839804636823960L;

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
