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
