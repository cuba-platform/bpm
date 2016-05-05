/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.entity.stencil;

public enum ServiceTaskStencilMethodArgType {

    STRING("String", "string"),
    BOOLEAN("Boolean", "boolean"),
    TEXT("Text", "text"),
    NUMBER("String", "number"),
    PROCESS_VARIABLE("String", "process_variable");

    private String propertyPackageType;
    private String customObjectType;

    ServiceTaskStencilMethodArgType(String propertyPackageType, String customObjectType) {
        this.propertyPackageType = propertyPackageType;
        this.customObjectType = customObjectType;
    }

    public String propertyPackageType() {
        return propertyPackageType;
    }

    public String customObjectType() {
        return customObjectType;
    }

    public static ServiceTaskStencilMethodArgType fromCustomObjectType(String customObjectType) {
        for (ServiceTaskStencilMethodArgType type : values()) {
            if (type.customObjectType().equals(customObjectType)) return type;
        }
        return null;
    }
}
