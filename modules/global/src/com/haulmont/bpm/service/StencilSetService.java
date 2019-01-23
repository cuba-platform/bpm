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

import com.haulmont.cuba.core.app.scheduled.MethodInfo;
import com.haulmont.cuba.core.entity.FileDescriptor;

import java.util.List;
import java.util.Map;

/**
 * Class that is used for storing and retrieving stencilSet json
 * see StencilSetManager class documentation for more details
 */
public interface StencilSetService {

    String NAME = "bpm_StencilSetService";

    String getStencilSet();

    void setStencilSet(String jsonData);

    void registerServiceTaskStencilBpmnJsonConverter(String stencilId);

    void resetStencilSet();

    byte[] exportStencilSet(String stencilsJson, List<FileDescriptor> iconFiles);

    void importStencilSet(byte[] zipBytes);

    Map<String, List<MethodInfo>> getAvailableBeans();
}
