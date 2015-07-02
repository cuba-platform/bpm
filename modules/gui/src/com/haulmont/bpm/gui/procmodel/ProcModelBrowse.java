/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.procmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.bpm.config.BpmConfig;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.bpm.gui.common.ProcDefinitionUtils;
import com.haulmont.bpm.gui.procdefinition.ProcDefinitionDeployWindow;
import com.haulmont.bpm.rest.RestModel;
import com.haulmont.bpm.service.ModelService;
import com.haulmont.bpm.service.ProcessRepositoryService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.export.ClosedDataProviderException;
import com.haulmont.cuba.gui.export.ExportDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ResourceException;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.security.global.UserSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.util.*;

/**
 * @author gorbunkov
 *         $Id$
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
    protected Table procModelsTable;

    @Inject
    protected ExportDisplay exportDisplay;

    @Inject
    protected FileUploadField modelUpload;

    @Inject
    protected FileUploadingAPI fileUploadingAPI;

    @Inject
    protected ModelService modelService;

    @Inject
    protected Metadata metadata;

    @Inject
    protected DataManager dataManager;

    protected static final Log log = LogFactory.getLog(ProcModelBrowse.class);

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
        procModelsTable.setItemClickAction(new BaseAction("openModeler") {
            @Override
            public void actionPerform(Component component) {
                openModeler();
            }
        });
        modelUpload.addListener(new ModeUploadListener());
    }

    public void openModeler() {
        _openModeler(procModelsDs.getItem());
    }

    protected void _openModeler(ProcModel procModel) {
        String webAppUrl = globalConfig.getWebAppUrl();
        String modelerUrl = bpmConfig.getModelerUrl();
        String url = webAppUrl + "/dispatch" + modelerUrl + "?modelId=" + procModel.getActModelId() + "&s=" + userSession.getId();
        showWebPage(url, Collections.<String, Object>singletonMap("tryToOpenAsPopup", Boolean.TRUE));
    }

    public void deploy() {
        openWindow("procDefinitionDeploy", WindowManager.OpenType.DIALOG,
                Collections.<String, Object>singletonMap("model", procModelsDs.getItem()));
    }

    public void exportModel() {
        ProcModel model = procModelsDs.getItem();
        exportDisplay.show(new ModelDataProvider(model), model.getName() + ".json");
    }

    public void copyModel() {
        ProcModel srcModel = procModelsDs.getItem();
        ProcModel modelCopy = metadata.create(ProcModel.class);
        modelCopy.setName(formatMessage("copyOf", srcModel.getName()));
        modelCopy.setDescription(modelCopy.getDescription());

        final Window.Editor editor = openEditor("bpm$ProcModel.edit", modelCopy, WindowManager.OpenType.THIS_TAB,
                Collections.<String, Object>singletonMap("srcModel", srcModel));
        editor.addListener(new CloseListener() {
            @Override
            public void windowClosed(String actionId) {
                if (COMMIT_ACTION_ID.equals(actionId)) {
                    procModelsDs.refresh();
                    _openModeler((ProcModel) editor.getItem());
                }
            }
        });
    }

    @Nullable
    protected ProcModel findModelByName(String modelName) {
        View view = new View(ProcModel.class)
                .addProperty("name")
                .addProperty("actModelId");
        LoadContext ctx = new LoadContext(ProcModel.class).setView(view);
        ctx.setQueryString("select m from bpm$ProcModel m where m.name = :name")
                .setParameter("name", modelName);
        return dataManager.load(ctx);
    }

    protected class ModelDataProvider implements ExportDataProvider {

        protected ProcModel procModel;

        protected InputStream inputStream;

        protected boolean closed;

        public ModelDataProvider(ProcModel procModel) {
            this.procModel = procModel;
        }

        @Override
        public InputStream provide() throws ResourceException, ClosedDataProviderException {
            if (closed)
                throw new ClosedDataProviderException();

            if (procModel == null)
                throw new IllegalArgumentException("Null process model passed");

            RestModel restModel = modelService.getModelJson(procModel.getActModelId());
            try {
                inputStream = new ByteArrayInputStream(restModel.getModelJson().getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                throw new BpmException("Unable to export process model", e);
            }

            return inputStream;
        }

        @Override
        public void close() {
            if (inputStream != null) {
                closed = true;
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("Error while closing file data provider", e);
                } finally {
                    inputStream = null;
                }
            }
        }
    }

    protected class ModeUploadListener extends FileUploadField.ListenerAdapter {
        @Override
        public void uploadSucceeded(Event event) {
            File file = fileUploadingAPI.getFile(modelUpload.getFileId());
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                final JsonNode modelJsonNode = objectMapper.readTree(file);
                JsonNode propertiesNode = modelJsonNode.get("properties");
                if (propertiesNode == null) {
                    showNotification(getMessage("invalidJsonDocument"), NotificationType.WARNING);
                    return;
                }
                JsonNode nameNode = propertiesNode.get("name");
                if (nameNode == null) {
                    showNotification(getMessage("invalidJsonDocument"), NotificationType.WARNING);
                    return;
                }
                final String modelName = nameNode.asText();
                final ProcModel existingModel = findModelByName(modelName);
                if (existingModel != null) {
                    showOptionDialog(getMessage("importModel.existsDialog.title"),
                            formatMessage("importModel.existsDialog.message", modelName),
                            MessageType.CONFIRMATION,
                            new Action[]{
                                    new DialogAction(DialogAction.Type.YES) {
                                        @Override
                                        public void actionPerform(Component component) {
                                            updateModel(modelJsonNode, existingModel);
                                        }
                                    },
                                    new DialogAction(DialogAction.Type.NO) {
                                    }
                            });
                } else {
                    createModel(modelJsonNode, modelName);
                }
            } catch (Exception e) {
                throw new BpmException("Error import model", e);
            }
        }

        private void createModel(JsonNode modelJsonNode, String modelName) {
            String actModelId = modelService.createModel(modelName);
            modelService.updateModel(actModelId, modelName, "", modelJsonNode.toString(), "");
            ProcModel procModel = metadata.create(ProcModel.class);
            procModel.setName(modelName);
            procModel.setActModelId(actModelId);
            procModelsDs.addItem(procModel);
            procModelsDs.commit();
            showNotification(getMessage("importModel.completed"), NotificationType.HUMANIZED);
        }

        private void updateModel(JsonNode modelJsonNode, ProcModel model) {
            modelService.updateModel(model.getActModelId(), model.getName(), "", modelJsonNode.toString(), "");
            showNotification(getMessage("importModel.completed"), NotificationType.HUMANIZED);
        }
    }
}