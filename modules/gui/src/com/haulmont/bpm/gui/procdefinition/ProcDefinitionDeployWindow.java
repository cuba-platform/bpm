/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.procdefinition;

import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;
import com.haulmont.bpm.service.ProcessRepositoryService;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
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
    protected LookupField processLookup;

    @WindowParam(name = "model", required = true)
    protected ProcModel model;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected ProcessRepositoryService processRepositoryService;

    public enum Decision {
        UPDATE_EXISTING,
        CREATE_NEW,
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
        List<ProcDefinition> procDefinitionsByModel = findProcDefinitionsByModel(model);
        if (procDefinitionsByModel.isEmpty()) {
            decisionOptionsGroup.setValue(Decision.CREATE_NEW);
        } else {
            decisionOptionsGroup.setValue(Decision.UPDATE_EXISTING);
            processLookup.setValue(procDefinitionsByModel.get(0));
        }
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
                ProcDefinition procDefinition = decisionOptionsGroup.getValue() == Decision.CREATE_NEW
                        ? null : (ProcDefinition) processLookup.getValue();
                final String processXml = processRepositoryService.convertModelToProcessXml(model.getActModelId());
                processRepositoryService.deployProcessFromXML(processXml, procDefinition, model);
                showNotification(getMessage("processDeployed"), NotificationType.HUMANIZED);
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

    protected List<ProcDefinition> findProcDefinitionsByModel(ProcModel model) {
        LoadContext ctx = new LoadContext(ProcDefinition.class);
        ctx.setQueryString("select pd from bpm$ProcDefinition pd where pd.model.id = :model order by pd.name")
                .setParameter("model", model);
        return dataManager.loadList(ctx);
    }


    public Decision getDecision() {
        return decisionOptionsGroup.getValue();
    }

    public ProcDefinition getProcDefinition() {
        return processLookup.getValue();
    }
}