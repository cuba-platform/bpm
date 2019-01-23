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

package com.haulmont.bpm.core;

import com.haulmont.cuba.core.entity.FileDescriptor;

import java.util.List;

/**
 * Class that is used for storing and retrieving stencilSet json
 */
public interface StencilSetManager {

    String NAME = "bpm_StencilSetManager";

    /**
     * Returns a stencilset that is a result of merge of the default stencilset from the 'stencilset.json' file
     * and the custom stencilset that is stored in the database
     * @return stencilset json
     */
    String getStencilSet();

    /**
     * Updates a custom stencilset in the database.
     * @param jsonData custom stencilset json. Note that custom stencilset must contain only custom stencils and propertyPackages
     */
    void setStencilSet(String jsonData);

    /**
     * Removes a custom stencil set from the database
     */
    void resetStencilSet();

    /**
     * Associates a new stencil with the {@link com.haulmont.bpm.core.jsonconverter.CustomServiceTaskJsonConverter}
     */
    void registerServiceTaskStencilBpmnJsonConverter(String stencilId);

    /**
     * Exports a stencilset json file and a set of stencil icon files into the zip archive
     * @param stencilsJson json string with the custom stencils
     * @param iconFiles a list of stencil icons file descriptors
     * @return a byte array of the zip archive
     */
    byte[] exportStencilSet(String stencilsJson, List<FileDescriptor> iconFiles);

    /**
     * Imports a zip archive with the stencilset.json and stencils icons
     * @param zipBytes a byte array of the zip archive
     */
    void importStencilSet(byte[] zipBytes);
}
