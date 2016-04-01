/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.procdefinition;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.OptionsGroup;
import com.haulmont.cuba.gui.components.actions.BaseAction;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class ProcDefinitionDeployWindow extends AbstractWindow {

    @Inject
    protected OptionsGroup decisionOptionsGroup;

    @Inject
    protected LookupField processLookup;

    @WindowParam(name = "model", required = true)
    protected ProcModel model;

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

        Map<String, Object> optionsMap = new HashMap<>();
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
        addAction(new BaseAction("windowCommit") {
            @Override
            public void actionPerform(Component component) {
                close(COMMIT_ACTION_ID);
            }

            @Override
            public String getCaption() {
                return getMessage("actions.Ok");
            }
        });

        addAction(new BaseAction("windowClose") {
            @Override
            public void actionPerform(Component component) {
                close(CLOSE_ACTION_ID);
            }

            @Override
            public String getCaption() {
                return getMessage("actions.Cancel");
            }
        });
    }

    public Decision getDecision() {
        return decisionOptionsGroup.getValue();
    }

    public ProcDefinition getProcDefinition() {
        return processLookup.getValue();
    }
}