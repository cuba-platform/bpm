/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.core.engine.scripting;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti.engine.impl.scripting.ScriptingEngines;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ExtScriptingEngines extends ScriptingEngines {

    protected Map<String, Object> standardBindingParams;

    public ExtScriptingEngines(ScriptBindingsFactory scriptBindingsFactory) {
        super(scriptBindingsFactory);
    }

    @Override
    protected Bindings createBindings(VariableScope variableScope) {
        Bindings bindings = super.createBindings(variableScope);
        for (Map.Entry<String, Object> entry : getStandardBindingParams().entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }
        return bindings;
    }

    @Override
    protected Bindings createBindings(VariableScope variableScope, boolean storeScriptVariables) {
        Bindings bindings = super.createBindings(variableScope, storeScriptVariables);
        for (Map.Entry<String, Object> entry : getStandardBindingParams().entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }
        return bindings;
    }

    protected Map<String, Object> getStandardBindingParams() {
        if (standardBindingParams == null) {
            standardBindingParams = new HashMap<>();
            standardBindingParams.put("metadata", AppBeans.get(Metadata.class));
            standardBindingParams.put("persistence", AppBeans.get(Persistence.class));
        }
        return standardBindingParams;
    }
}