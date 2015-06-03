/*
 * Copyright (c) 2015 com.haulmont.bpm.gui.procdefinition
 */
package com.haulmont.bpm.gui.procdefinition;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.data.ValueListener;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 */
public class ProcDefinitionDeployWindow extends AbstractWindow {

    @Inject
    protected OptionsGroup decisionOptionsGroup;

    @Inject
    protected Label messageLabel;

    @Inject
    protected LookupField processLookup;

    @WindowParam(name = "procDefinitions", required = true)
    List<ProcDefinition> procDefinitions;

    public enum Decision {
        UPDATE_EXISTING,
        CREATE_NEW,
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        initWindowActions();

        messageLabel.setValue(formatMessage("processesExist", procDefinitions.get(0).getActKey()));

        processLookup.setOptionsList(procDefinitions);
        processLookup.setValue(procDefinitions.get(0));

        Map<String, Object> optionsMap = new HashMap<>();
        for (Decision decision : Decision.values()) {
            optionsMap.put(getMessage(decision.name()), decision);
        }

        decisionOptionsGroup.setOptionsMap(optionsMap);
        decisionOptionsGroup.setValue(Decision.UPDATE_EXISTING);
        decisionOptionsGroup.addListener(new ValueListener() {
            @Override
            public void valueChanged(Object source, String property, Object prevValue, Object value) {
                processLookup.setEnabled(value == Decision.UPDATE_EXISTING);
                processLookup.setRequired(value == Decision.UPDATE_EXISTING);
            }
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