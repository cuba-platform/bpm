/*
 * Copyright (c) 115 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */
package com.haulmont.bpm.gui.procmodel;

import com.haulmont.bpm.config.BpmConfig;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;
import com.haulmont.bpm.gui.common.ProcDefinitionUtils;
import com.haulmont.bpm.gui.procdefinition.ProcDefinitionDeployWindow;
import com.haulmont.bpm.service.ProcessRepositoryService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.security.global.UserSession;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author gorbunkov
 * $Id$
 */
public class ProcModelBrowse extends AbstractLookup {

    @Named("procModelsTable.create")
    protected CreateAction procModelsTableCreate;

    @Named("procModelsTable.edit")
    protected EditAction procModelsTableEdit;

    @Inject
    protected UserSession userSession;

    @Inject
    protected GlobalConfig globalConfig;

    @Inject
    protected BpmConfig bpmConfig;

    @Inject
    protected CollectionDatasource<ProcModel, UUID> procModelsDs;

    @Inject
    protected ProcessRepositoryService processRepositoryService;

    @Inject
    protected Table procModelsTable;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        procModelsTableCreate.setOpenType(WindowManager.OpenType.DIALOG);
        procModelsTableEdit.setOpenType(WindowManager.OpenType.DIALOG);
        procModelsTableCreate.setAfterCommitHandler(new CreateAction.AfterCommitHandler() {
            @Override
            public void handle(Entity entity) {
                _openModeler((ProcModel) entity);
            }
        });
    }

    public void openModeler() {
        _openModeler(procModelsDs.getItem());
    }

    protected void _openModeler(ProcModel procModel) {
        String webAppUrl = globalConfig.getWebAppUrl();
        String modelerUrl = bpmConfig.getModelerUrl();
        StringBuilder url = new StringBuilder();
        url.append(webAppUrl)
                .append("/dispatch")
                .append(modelerUrl)
                .append("?modelId=")
                .append(procModel.getActModelId())
                .append("&s=")
                .append(userSession.getId());
        showWebPage(url.toString(), Collections.<String, Object>singletonMap("tryToOpenAsPopup", Boolean.TRUE));
    }

    public void deploy() {
        final String processXml = processRepositoryService.getProcessDefinitionXmlFromModel(procModelsDs.getItem().getActModelId());
        String processKey = ProcDefinitionUtils.getProcessKeyFromXml(processXml);
        List<ProcDefinition> procDefinitionsWithTheSameKey = processRepositoryService.getProcDefinitionsByProcessKey(processKey);
        if (procDefinitionsWithTheSameKey.isEmpty()) {
            ProcDefinition procDefinition = processRepositoryService.deployProcessFromXML(processXml, null);
            showNotification(getMessage("processDeployed"), NotificationType.HUMANIZED);
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("procDefinitions", procDefinitionsWithTheSameKey);
            final ProcDefinitionDeployWindow deployWindow = openWindow("procDefinitionDeploy", WindowManager.OpenType.DIALOG, params);
            deployWindow.addListener(new CloseListener() {
                @Override
                public void windowClosed(String actionId) {
                    if (COMMIT_ACTION_ID.equals(actionId)) {
                        if (ProcDefinitionDeployWindow.Decision.UPDATE_EXISTING == deployWindow.getDecision()) {
                            ProcDefinition procDefinition = processRepositoryService.deployProcessFromXML(processXml, deployWindow.getProcDefinition());
                            showNotification(getMessage("processUploaded"), NotificationType.HUMANIZED);
                        } else {
                            ProcDefinition procDefinition = processRepositoryService.deployProcessFromXML(processXml, null);
                            showNotification(getMessage("processUploaded"), NotificationType.HUMANIZED);
                        }
                    }
                }
            });
        }
    }
}