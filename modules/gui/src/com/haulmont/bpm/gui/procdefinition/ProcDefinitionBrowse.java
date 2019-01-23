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

import com.google.common.base.Strings;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.service.ProcessRepositoryService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

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

        deployUpload.addFileUploadSucceedListener(new UploadSucceedListener());
        deployUpload.addFileUploadErrorListener(new UploadErrorListener());
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

    protected class UploadSucceedListener implements Consumer<FileUploadField.FileUploadSucceedEvent> {

        public UploadSucceedListener() {
        }

        @Override
        public void accept(FileUploadField.FileUploadSucceedEvent e) {
            File file = fileUploadingAPI.getFile(deployUpload.getFileId());

            String extension = FilenameUtils.getExtension(deployUpload.getFileName());
            if (!"xml".equalsIgnoreCase(extension)) {
                showNotification(getMessage("fileNotXml"), NotificationType.ERROR);
                return;
            }

            String processXml;
            try {
                processXml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new RuntimeException("Process upload error", ex);
            }

            String processId = getProcessName(processXml);
            List<ProcDefinition> procDefinitionsWithTheSameName = getProcDefinitionsByName(processId);
            if (procDefinitionsWithTheSameName.isEmpty()) {
                ProcDefinition procDefinition = processRepositoryService.deployProcessFromXml(processXml, null, null);
                procDefinitionsDs.addItem(procDefinition);
                showNotification(getMessage("processUploaded"), NotificationType.HUMANIZED);
            } else {
                ProcDefinitionDeployWindow deployWindow = (ProcDefinitionDeployWindow) openWindow("procDefinitionDeploy",
                        OpenType.DIALOG,
                        ParamsMap.of("selectedProcDefinition", procDefinitionsWithTheSameName.get(0)));

                deployWindow.addCloseListener(actionId -> {
                    if (COMMIT_ACTION_ID.equals(actionId)) {
                        if (ProcDefinitionDeployWindow.Decision.UPDATE_EXISTING == deployWindow.getDecision()) {
                            ProcDefinition procDefinition = processRepositoryService.deployProcessFromXml(processXml,
                                    deployWindow.getProcDefinition(), null);
                            procDefinitionsDs.updateItem(procDefinition);
                            showNotification(getMessage("processUploaded"), NotificationType.HUMANIZED);
                        } else {
                            ProcDefinition procDefinition = processRepositoryService.deployProcessFromXml(processXml, null, null);
                            procDefinitionsDs.addItem(procDefinition);
                            showNotification(getMessage("processUploaded"), NotificationType.HUMANIZED);
                        }
                    }
                });
            }
        }
    }

    protected class UploadErrorListener implements Consumer<FileUploadField.FileUploadErrorEvent> {

        @Override
        public void accept(UploadField.FileUploadErrorEvent e) {
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