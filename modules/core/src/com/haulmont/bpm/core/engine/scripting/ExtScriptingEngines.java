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
