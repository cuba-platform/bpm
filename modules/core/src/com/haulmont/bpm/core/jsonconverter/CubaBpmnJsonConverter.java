/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core.jsonconverter;

import org.activiti.editor.language.json.converter.BaseBpmnJsonConverter;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;

/**
 * A BpmnJsonConverter that can register custom stencils
 */
public class CubaBpmnJsonConverter extends BpmnJsonConverter {

    public static void addConvertersToBpmnMapItem(String stencilId, Class<? extends BaseBpmnJsonConverter> converterClass) {
        convertersToBpmnMap.put(stencilId, converterClass);
        BpmnJsonConverter.DI_RECTANGLES.add(stencilId);
    }
}
