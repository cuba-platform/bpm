/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.procdefinition;

import com.google.common.base.Strings;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.service.ProcessRepositoryService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author gorbunkov
 */
public class ProcDefinitionBrowse extends AbstractLookup {

    @Inject
    protected FileUploadField deployUpload;

    @Inject
    protected FileUploadingAPI fileUploadingAPI;

    @Inject
    protected ProcessRepositoryService processRepositoryService;

    @Inject
    protected CollectionDatasource<ProcDefinition, UUID> procDefinitionsDs;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected ProcessRuntimeService processRuntimeService;

    @Inject
    protected Table procDefinitionTable;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        procDefinitionTable.addAction(new ProcDefinitionRemoveAction(procDefinitionTable));
        deployUpload.addListener(new UploadListener());
    }

    protected List<ProcDefinition> getProcDefinitionsByName(String name) {
        LoadContext ctx = new LoadContext(ProcDefinition.class);
        ctx.setQuery(new LoadContext.Query("select pd from bpm$ProcDefinition pd where pd.name = :name")
                .setParameter("name", name));
        return dataManager.loadList(ctx);
    }

    protected String getProcessName(String xml) {
        Document document = Dom4j.readDocument(xml);
        Element process = document.getRootElement().element("process");
        if (process == null) {
            throw new IllegalArgumentException("Process xml doesn't contain 'process' element");
        }

        String name = process.attributeValue("name");
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Process name is not defined");
        }

        return name;
    }

    protected class UploadListener extends FileUploadField.ListenerAdapter {
        @Override
        public void uploadSucceeded(Event event) {
            File file = fileUploadingAPI.getFile(deployUpload.getFileId());
            try {
                final String processXml = FileUtils.readFileToString(file);
                String processId = getProcessName(processXml);
                List<ProcDefinition> procDefinitionsWithTheSameName = getProcDefinitionsByName(processId);
                if (procDefinitionsWithTheSameName.isEmpty()) {
                    ProcDefinition procDefinition = processRepositoryService.deployProcessFromXml(processXml, null, null);
                    procDefinitionsDs.addItem(procDefinition);
                    showNotification(getMessage("processUploaded"), NotificationType.HUMANIZED);
                } else {
                    Map<String, Object> params = new HashMap<>();
                    params.put("selectedProcDefinition", procDefinitionsWithTheSameName.get(0));
                    final ProcDefinitionDeployWindow deployWindow = (ProcDefinitionDeployWindow) openWindow("procDefinitionDeploy", WindowManager.OpenType.DIALOG, params);
                    deployWindow.addListener(new CloseListener() {
                        @Override
                        public void windowClosed(String actionId) {
                            if (COMMIT_ACTION_ID.equals(actionId)) {
                                if (ProcDefinitionDeployWindow.Decision.UPDATE_EXISTING == deployWindow.getDecision()) {
                                    ProcDefinition procDefinition = processRepositoryService.deployProcessFromXml(processXml, deployWindow.getProcDefinition(), null);
                                    procDefinitionsDs.updateItem(procDefinition);
                                    showNotification(getMessage("processUploaded"), NotificationType.HUMANIZED);
                                } else {
                                    ProcDefinition procDefinition = processRepositoryService.deployProcessFromXml(processXml, null, null);
                                    procDefinitionsDs.addItem(procDefinition);
                                    showNotification(getMessage("processUploaded"), NotificationType.HUMANIZED);
                                }
                            }
                        }
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException("Process upload error", e);
            }
        }

        @Override
        public void uploadFailed(Event event) {
            showNotification(getMessage("processUploadFailed"), NotificationType.ERROR);
        }
    }

    protected class ProcDefinitionRemoveAction extends RemoveAction {

        public ProcDefinitionRemoveAction(ListComponent target) {
            super(target);
        }

        @Override
        protected void doRemove(Set selected, boolean autocommit) {
            long activeProcessesCount = processRuntimeService.getActiveProcessesCount(procDefinitionsDs.getItem());
            if (activeProcessesCount > 0) {
                showNotification(getMessage("cannotRemoveActiveProcesses"), NotificationType.ERROR);
            } else {
                super.doRemove(selected, autocommit);
                for (Object selectedItem : selected) {
                    processRepositoryService.undeployProcess(((ProcDefinition)selectedItem).getActId());
                }
            }
        }
    }
}