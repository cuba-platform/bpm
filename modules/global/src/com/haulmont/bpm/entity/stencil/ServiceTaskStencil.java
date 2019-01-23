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
    protected List<StencilMethodArg> methodArgs = new ArrayList<>();

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

    public List<StencilMethodArg> getMethodArgs() {
        return methodArgs;
    }

    public void setMethodArgs(List<StencilMethodArg> methodArgs) {
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
