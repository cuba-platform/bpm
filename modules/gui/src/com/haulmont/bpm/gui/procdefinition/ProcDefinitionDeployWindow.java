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

package com.haulmont.bpm.gui.procdefinition;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.OptionsGroup;
import com.haulmont.cuba.gui.components.actions.BaseAction;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProcDefinitionDeployWindow extends AbstractWindow {
    @Inject
    protected OptionsGroup<Decision, Decision> decisionOptionsGroup;
    @Inject
    protected LookupField processLookup;

    @WindowParam(name = "selectedProcDefinition")
    protected ProcDefinition selectedProcDefinition;

    public enum Decision {
        CREATE_NEW,
        UPDATE_EXISTING,
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        initWindowActions();

        Map<String, Decision> optionsMap = new LinkedHashMap<>();
        for (Decision decision : Decision.values()) {
            optionsMap.put(getMessage(decision.name()), decision);
        }

        decisionOptionsGroup.setOptionsMap(optionsMap);
        if (selectedProcDefinition == null) {
            decisionOptionsGroup.setValue(Decision.CREATE_NEW);
        } else {
            decisionOptionsGroup.setValue(Decision.UPDATE_EXISTING);
            processLookup.setValue(selectedProcDefinition);
        }
        decisionOptionsGroup.addValueChangeListener(e -> {
            processLookup.setEnabled(e.getValue() == Decision.UPDATE_EXISTING);
            processLookup.setRequired(e.getValue() == Decision.UPDATE_EXISTING);
        });
    }

    protected void initWindowActions() {
        addAction(new BaseAction("windowCommit")
            .withHandler(event -> close(COMMIT_ACTION_ID))
            .withCaption(getMessage("actions.Ok")));

        addAction(new BaseAction("windowClose")
                .withHandler(event -> close(CLOSE_ACTION_ID))
                .withCaption(getMessage("actions.Cancel")));
    }

    public Decision getDecision() {
        return (Decision) decisionOptionsGroup.getValue();
    }

    public ProcDefinition getProcDefinition() {
        return (ProcDefinition) processLookup.getValue();
    }
}