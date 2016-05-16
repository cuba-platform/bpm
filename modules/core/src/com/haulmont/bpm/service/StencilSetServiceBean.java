/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.core.StencilSetManager;
import com.haulmont.bpm.core.StencilsBeansMetadata;
import com.haulmont.cuba.core.app.scheduled.MethodInfo;
import com.haulmont.cuba.core.entity.FileDescriptor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Service(StencilSetService.NAME)
public class StencilSetServiceBean implements StencilSetService {

    @Inject
    protected StencilSetManager stencilSetManager;

    @Inject
    protected StencilsBeansMetadata stencilsBeansMetadata;

    @Override
    public String getStencilSet() {
        return stencilSetManager.getStencilSet();
    }

    @Override
    public void setStencilSet(String jsonData) {
        stencilSetManager.setStencilSet(jsonData);
    }

    @Override
    public void registerServiceTaskStencilBpmnJsonConverter(String stencilId) {
        stencilSetManager.registerServiceTaskStencilBpmnJsonConverter(stencilId);
    }

    @Override
    public void resetStencilSet() {
        stencilSetManager.resetStencilSet();
    }

    @Override
    public byte[] exportStencilSet(String stencilsJson, List<FileDescriptor> iconFiles) {
        return stencilSetManager.exportStencilSet(stencilsJson, iconFiles);
    }

    @Override
    public void importStencilSet(byte[] zipBytes) {
        stencilSetManager.importStencilSet(zipBytes);
    }

    @Override
    public Map<String, List<MethodInfo>> getAvailableBeans() {
        return stencilsBeansMetadata.getAvailableBeans();
    }
}
