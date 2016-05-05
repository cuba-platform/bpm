/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.service;

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
}
