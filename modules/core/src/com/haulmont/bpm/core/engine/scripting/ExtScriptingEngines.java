/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core.engine.scripting;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Resources;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti.engine.impl.scripting.ScriptingEngines;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.Map;

/**
 * Modified ScriptingEngines adds platform infrastructure objects (persistence, metadata, etc.)
 * to the script binding
 */
public class ExtScriptingEngines extends ScriptingEngines {

    protected Map<String, Object> standardBindingParams;

    public ExtScriptingEngines(ScriptBindingsFactory scriptBindingsFactory) {
        super(scriptBindingsFactory);
    }

    @Override
    protected Object evaluate(String script, String language, Bindings bindings) {
        Resources resources = AppBeans.get(Resources.class);
        String scriptByPath = resources.getResourceAsString(script);
        if (scriptByPath != null) script = scriptByPath;
        return super.evaluate(script, language, bindings);
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
