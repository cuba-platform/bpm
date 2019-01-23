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

package com.haulmont.bpm.gui.procmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.bali.util.Preconditions;
import com.haulmont.bpm.config.BpmConfig;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.bpm.gui.procdefinition.ProcDefinitionDeployWindow;
import com.haulmont.bpm.rest.RestModel;
import com.haulmont.bpm.service.ModelService;
import com.haulmont.bpm.service.ProcessRepositoryService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.Action.Status;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.export.ExportDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.security.global.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Collections.singletonMap;

public class ProcModelBrowse extends AbstractLookup {

    protected static final Logger log = LoggerFactory.getLogger(ProcModelBrowse.class);

    @Named("procModelsTable.create")
    protected CreateAction procModelsTableCreate;

    @Inject
    private Button removeBtn;

    @Inject
    protected UserSession userSession;

    @Inject
    protected BpmConfig bpmConfig;

    @Inject
    protected CollectionDatasource<ProcModel, UUID> procModelsDs;

    @Inject
    protected Table<ProcModel> procModelsTable;

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

    @Inject
    protected ProcessRepositoryService processRepositoryService;

    @Named("procModelsTable.openModeler")
    protected Action procModelsTableOpenModeler;

    @Inject
    protected Companion companion;

    @Inject
    protected Button openModelerBtn;

    public interface Companion {
        void openModeler(String modelerUrl);

        void setupModelerPopupOpener(Button button, String modelerUrl);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        procModelsTableCreate.setOpenType(WindowManager.OpenType.DIALOG);
        procModelsTableCreate.setAfterCommitHandler(entity ->
                _openModeler((ProcModel) entity)
        );
        procModelsTable.setItemClickAction(procModelsTableOpenModeler);
        modelUpload.addFileUploadSucceedListener(new ModelUploadListener());

        //the remove action should reload model entities, because they may be modified when the model is saved from the
        //model editor
        RemoveAction removeAction = new RemoveAction(procModelsTable) {
            @Override
            protected void doRemove(Set<Entity> selected, boolean autocommit) {
                CollectionDatasource<ProcModel, UUID> datasource = target.getDatasource();
                datasource.refresh();
                for (Entity entity : selected) {
                    Entity reloadedEntity = datasource.getItem((UUID) entity.getId());
                    if (reloadedEntity != null) {
                        datasource.removeItem((ProcModel) reloadedEntity);
                    }
                }
                datasource.commit();
            }
        };

        procModelsTable.addAction(removeAction);
        removeBtn.setAction(removeAction);

        procModelsDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                String modelerUrl = generateModelerUrl(e.getItem());
                companion.setupModelerPopupOpener(openModelerBtn, modelerUrl);
            }
        });
    }

    protected String generateModelerUrl(ProcModel procModel) {
        return "dispatch" + bpmConfig.getModelerUrl() + "?modelId=" + procModel.getActModelId() + "&s=" + userSession.getId();
    }

    public void openModeler() {
        _openModeler(procModelsDs.getItem());
    }

    protected void _openModeler(ProcModel procModel) {
        companion.openModeler(generateModelerUrl(procModel));
    }

    public void deploy() {
        Map<String, Object> params = new HashMap<>();
        List<ProcDefinition> procDefinitionsByModel = findProcDefinitionsByModel(procModelsDs.getItem());
        if (!procDefinitionsByModel.isEmpty()) {
            params.put("selectedProcDefinition", procDefinitionsByModel.get(0));
        }
        ProcDefinitionDeployWindow deployWindow =
                (ProcDefinitionDeployWindow) openWindow("procDefinitionDeploy", WindowManager.OpenType.DIALOG, params);
        deployWindow.addCloseListener(actionId -> {
            if (COMMIT_ACTION_ID.equals(actionId)) {
                ProcDefinition procDefinition = deployWindow.getDecision() == ProcDefinitionDeployWindow.Decision.UPDATE_EXISTING
                        ? deployWindow.getProcDefinition() : null;
                final String processXml = processRepositoryService.convertModelToProcessXml(procModelsDs.getItem().getActModelId());
                processRepositoryService.deployProcessFromXml(processXml, procDefinition, procModelsDs.getItem());
                showNotification(getMessage("processDeployed"), NotificationType.HUMANIZED);
            }
        });
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

        Editor editor = openEditor("bpm$ProcModel.edit", modelCopy, WindowManager.OpenType.THIS_TAB,
                singletonMap("srcModel", srcModel));
        editor.addCloseListener(actionId -> {
            if (COMMIT_ACTION_ID.equals(actionId)) {
                procModelsDs.refresh();
                _openModeler((ProcModel) editor.getItem());
            }
        });
    }

    @Nullable
    protected ProcModel findModelByName(String modelName) {
        View view = new View(ProcModel.class)
                .addProperty("name")
                .addProperty("actModelId");
        LoadContext<ProcModel> ctx = LoadContext.create(ProcModel.class).setView(view);
        ctx.setQueryString("select m from bpm$ProcModel m where m.name = :name")
                .setParameter("name", modelName);
        return dataManager.load(ctx);
    }

    protected List<ProcDefinition> findProcDefinitionsByModel(ProcModel model) {
        LoadContext<ProcDefinition> ctx = LoadContext.create(ProcDefinition.class);
        ctx.setQueryString(
                "select pd from bpm$ProcDefinition pd " +
                "where pd.model.id = :modelId order by pd.name, pd.deploymentDate desc")
                .setParameter("modelId", model.getId());
        return dataManager.loadList(ctx);
    }

    protected class ModelDataProvider implements ExportDataProvider {

        protected ProcModel procModel;

        public ModelDataProvider(ProcModel procModel) {
            this.procModel = procModel;

            Preconditions.checkNotNullArgument(procModel, "Null process model passed");
        }

        @Override
        public InputStream provide() {
            RestModel restModel = modelService.getModelJson(procModel.getActModelId());
            byte[] bytes = restModel.getModelJson().getBytes(StandardCharsets.UTF_8);
            return new ByteArrayInputStream(bytes);
        }
    }

    protected class ModelUploadListener implements Consumer<FileUploadField.FileUploadSucceedEvent> {

        public ModelUploadListener() {
        }

        @Override
        public void accept(FileUploadField.FileUploadSucceedEvent e) {
            File file = fileUploadingAPI.getFile(modelUpload.getFileId());
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode modelJsonNode = objectMapper.readTree(file);
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

                String modelName = nameNode.asText();
                ProcModel existingModel = findModelByName(modelName);
                if (existingModel != null) {
                    showOptionDialog(getMessage("importModel.existsDialog.title"),
                            formatMessage("importModel.existsDialog.message", modelName),
                            MessageType.CONFIRMATION,
                            new Action[]{
                                    new DialogAction(Type.YES) {
                                        @Override
                                        public void actionPerform(Component component) {
                                            updateModel(modelJsonNode, existingModel);
                                        }
                                    },
                                    new DialogAction(Type.NO, Status.PRIMARY)
                            });
                } else {
                    createModel(modelJsonNode, modelName);
                }
            } catch (Exception ex) {
                throw new BpmException("Error import model", ex);
            }
        }

        protected void createModel(JsonNode modelJsonNode, String modelName) {
            String actModelId = modelService.createModel(modelName);
            modelService.updateModel(actModelId, modelName, "", modelJsonNode.toString(), "");
            ProcModel procModel = metadata.create(ProcModel.class);
            procModel.setName(modelName);
            procModel.setActModelId(actModelId);
            procModelsDs.addItem(procModel);
            procModelsDs.commit();
            showNotification(getMessage("importModel.completed"), NotificationType.HUMANIZED);
        }

        protected void updateModel(JsonNode modelJsonNode, ProcModel model) {
            modelService.updateModel(model.getActModelId(), model.getName(), "", modelJsonNode.toString(), "");
            showNotification(getMessage("importModel.completed"), NotificationType.HUMANIZED);
        }
    }
}