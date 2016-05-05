/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.entity.stencil;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.FileDescriptor;

import javax.annotation.PostConstruct;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MetaClass(name = "bpm$ServiceTaskStencil")
public class ServiceTaskStencil extends Stencil {

    @MetaProperty(mandatory = true)
    protected String beanName;

    @MetaProperty(mandatory = true)
    protected String methodName;

    @MetaProperty
    protected UUID iconFileId;

    @MetaProperty
    protected FileDescriptor iconFile;

    @MetaProperty
    @OneToMany(mappedBy = "stencil")
    protected List<ServiceTaskStencilMethodArg> methodArgs = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        editable = true;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<ServiceTaskStencilMethodArg> getMethodArgs() {
        return methodArgs;
    }

    public void setMethodArgs(List<ServiceTaskStencilMethodArg> methodArgs) {
        this.methodArgs = methodArgs;
    }

    public UUID getIconFileId() {
        return iconFileId;
    }

    public void setIconFileId(UUID iconFileId) {
        this.iconFileId = iconFileId;
    }

    public FileDescriptor getIconFile() {
        return iconFile;
    }

    public void setIconFile(FileDescriptor iconFile) {
        this.iconFile = iconFile;
    }
}
