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

package com.haulmont.bpm.gui.stencilset;

import com.haulmont.bpm.entity.stencil.GroupStencil;
import com.haulmont.bpm.entity.stencil.ServiceTaskStencil;
import com.haulmont.bpm.entity.stencil.StandardStencil;
import com.haulmont.bpm.entity.stencil.Stencil;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.bpm.gui.stencilset.frame.AbstractStencilFrame;
import com.haulmont.bpm.gui.stencilset.frame.ServiceTaskStencilFrame;
import com.haulmont.bpm.gui.stencilset.helper.StencilSetJsonHelper;
import com.haulmont.bpm.service.StencilSetService;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class StencilSetEditor extends AbstractWindow {

    @Inject
    protected HierarchicalDatasource<Stencil, UUID> stencilsDs;

    @Inject
    protected TreeTable<Stencil> stencilsTable;

    @Inject
    protected CollectionDatasource<GroupStencil, UUID> groupsDs;

    @Inject
    protected ServiceTaskStencilFrame serviceTaskStencilFrame;

    @Inject
    protected AbstractStencilFrame standardStencilFrame;

    @Inject
    protected AbstractStencilFrame groupStencilFrame;

    @Inject
    protected Metadata metadata;

    @Inject
    protected StencilSetService stencilSetService;

    @Inject
    protected Button upBtn;

    @Inject
    protected Button downBtn;

    @Inject
    protected ExportDisplay exportDisplay;

    @Inject
    protected FileUploadField importUpload;

    @Inject
    protected FileUploadingAPI fileUploadingAPI;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected Button removeBtn;

    protected AbstractStencilFrame activeStencilFrame;

    protected boolean treeItemChangeListenerEnabled = true;

    protected boolean removingStencil = false;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        initData();

        stencilsDs.addItemChangeListener(e -> {
            if (!treeItemChangeListenerEnabled) return;

            Stencil item = e.getItem();

            if (activeStencilFrame != null && !removingStencil) {
                if (!validateActiveStencilFrame()) {
                    treeItemChangeListenerEnabled = false;
                    stencilsTable.setSelected(e.getPrevItem());
                    treeItemChangeListenerEnabled = true;
                    return;
                }
            }

            serviceTaskStencilFrame.setVisible(false);
            standardStencilFrame.setVisible(false);
            groupStencilFrame.setVisible(false);

            if (item != null) {
                if (item instanceof ServiceTaskStencil) {
                    activeStencilFrame = serviceTaskStencilFrame;
                } else if (item instanceof StandardStencil) {
                    activeStencilFrame = standardStencilFrame;
                } else if (item instanceof GroupStencil) {
                    activeStencilFrame = groupStencilFrame;
                }

                activeStencilFrame.setVisible(true);
                activeStencilFrame.setStencil(item);
            }
        });

        RemoveAction stencilsTableRemoveAction = new RemoveAction(stencilsTable) {

            @Override
            protected void doRemove(Set selected, boolean autocommit) {
                removingStencil = true;
                super.doRemove(selected, autocommit);
                removingStencil = false;
            }

            @Override
            protected boolean isApplicable() {
                Stencil selected = (Stencil) target.getSingleSelected();
                boolean isGroupAndHasChildren = (selected instanceof GroupStencil) && !stencilsDs.getChildren(selected.getId()).isEmpty();
                return super.isApplicable()
                        && selected.getEditable()
                        && !isGroupAndHasChildren;
            }
        };
        stencilsTableRemoveAction.setAutocommit(false);
        stencilsTable.addAction(stencilsTableRemoveAction);
        removeBtn.setAction(stencilsTableRemoveAction);

        MoveStencilUpAction moveStencilUpAction = new MoveStencilUpAction();
        upBtn.setAction(moveStencilUpAction);
        stencilsTable.addAction(moveStencilUpAction);

        MoveStencilDownAction moveStencilDownAction = new MoveStencilDownAction();
        downBtn.setAction(moveStencilDownAction);
        stencilsTable.addAction(moveStencilDownAction);

        initImportUpload();
    }

    protected void initImportUpload() {
        importUpload.addFileUploadSucceedListener(e -> {
            File file = fileUploadingAPI.getFile(importUpload.getFileId());
            if (file == null) {
                throw new BpmException("Error on stencil set import. File is null.");
            }
            try {
                stencilSetService.importStencilSet(Files.readAllBytes(file.toPath()));
                initData();
                showNotification(getMessage("importSuccessful"), NotificationType.HUMANIZED);
            } catch (IOException e1) {
                throw new BpmException(e1);
            }
        });
    }

    protected void initData() {
        groupsDs.clear();
        stencilsDs.clear();
        String srcStencilSetJson = stencilSetService.getStencilSet();
        List<Stencil> stencils = StencilSetJsonHelper.parseStencilSetJson(srcStencilSetJson);
        for (Stencil stencil : stencils) {
            stencilsDs.addItem(stencil);
            if (stencil instanceof GroupStencil) {
                groupsDs.addItem((GroupStencil) stencil);
            }
        }
    }

    public void createGroupStencil() {
        if (!validateActiveStencilFrame()) return;
        GroupStencil groupStencil = metadata.create(GroupStencil.class);
        groupStencil.setEditable(true);
        stencilsDs.addItem(groupStencil);
        stencilsTable.setSelected(groupStencil);
        activeStencilFrame.requestFocus();
        groupsDs.addItem(groupStencil);
    }

    public void createServiceTaskStencil() {
        if (!validateActiveStencilFrame()) return;
        GroupStencil group = null;
        Stencil currentlySelected = stencilsDs.getItem();
        if (currentlySelected != null) {
            if (currentlySelected instanceof GroupStencil) {
                group = (GroupStencil) currentlySelected;
            } else {
                group = currentlySelected.getParentGroup();
            }
        }

        if (group == null) {
            showNotification(getMessage("selectGroup"), NotificationType.WARNING);
            return;
        }

        ServiceTaskStencil stencilEntity = metadata.create(ServiceTaskStencil.class);
        stencilEntity.setOrderNo(0);

        GroupStencil finalGroup = group;
        stencilsDs.getItems().stream()
                .filter(stencil -> finalGroup.equals(stencil.getParentGroup()))
                .max((o1, o2) -> o1.getOrderNo() - o2.getOrderNo())
                .ifPresent(stencilWithMaxOrderNo -> stencilEntity.setOrderNo(stencilWithMaxOrderNo.getOrderNo() + 1));

        stencilEntity.setParentGroup(group);
        stencilsDs.addItem(stencilEntity);
        stencilsTable.setSelected(stencilEntity);
        activeStencilFrame.requestFocus();
        stencilsTable.expand(group.getId());
    }

    public void saveStencilSet() throws IOException {
        if (!validateActiveStencilFrame()) return;
        String modifiedStencilSetJson = StencilSetJsonHelper.generateCustomStencilSet(stencilsDs.getItems());
        stencilSetService.setStencilSet(modifiedStencilSetJson);
        stencilsDs.getItems().stream()
                .filter(stencil -> stencil instanceof ServiceTaskStencil)
                .forEach(stencil -> stencilSetService.registerServiceTaskStencilBpmnJsonConverter(stencil.getStencilId()));
        showNotification(getMessage("saved"), NotificationType.HUMANIZED);
    }

    public void close() {
        close(CLOSE_ACTION_ID, true);
    }

    public void exportStencilSet() throws IOException {
        String customStencilSetJson = StencilSetJsonHelper.generateCustomStencilSet(stencilsDs.getItems());
        List<FileDescriptor> icons = stencilsDs.getItems().stream()
                .filter(stencil -> stencil instanceof ServiceTaskStencil
                        && ((ServiceTaskStencil) stencil).getIconFileId() != null)
                .map(stencil -> {
                    if (((ServiceTaskStencil)stencil).getIconFile() != null) {
                        return ((ServiceTaskStencil)stencil).getIconFile();
                    } else {
                        LoadContext<FileDescriptor> ctx = new LoadContext<>(FileDescriptor.class).setId(((ServiceTaskStencil) stencil).getIconFileId());
                        return dataManager.load(ctx);
                    }
                })
                .collect(Collectors.toList());
        byte[] bytes = stencilSetService.exportStencilSet(customStencilSetJson, icons);
        exportDisplay.show(new ByteArrayDataProvider(bytes), "stencilset.zip");
    }

    public void resetStencilSet() {
        showOptionDialog(getMessage("resetStencilSetDlg.caption"),
                getMessage("resetStencilSetDlg.message"),
                MessageType.CONFIRMATION,
                new Action[]{
                        new DialogAction(DialogAction.Type.YES) {
                            @Override
                            public void actionPerform(Component component) {
                                _resetStencilSet();
                                initData();
                            }
                        },
                        new DialogAction(DialogAction.Type.NO) {
                            @Override
                            public void actionPerform(Component component) {
                            }
                        }
                });
    }

    protected void _resetStencilSet() {
        groupsDs.clear();
        stencilsDs.clear();
        stencilSetService.resetStencilSet();
    }

    protected boolean validateActiveStencilFrame() {
        if (activeStencilFrame != null) {
            List<Validatable> validatables = new ArrayList<>();
            Collection<Component> frameComponents = ComponentsHelper.getComponents(activeStencilFrame);
            for (Component frameComponent : frameComponents) {
                if (frameComponent instanceof Validatable) {
                    validatables.add((Validatable) frameComponent);
                }
            }
            return validate(validatables);
        }
        return true;
    }

    protected void refreshDs() {
        Collection<Stencil> items = new ArrayList<>(stencilsDs.getItems());
        stencilsDs.clear();

        Map<Stencil, List<Stencil>> groupMap = new LinkedHashMap<>();
        items.stream().filter(stencil -> stencil instanceof GroupStencil)
                .forEach(groupStencil -> groupMap.put(groupStencil, new ArrayList<>()));

        items.stream().filter(stencil -> !(stencil instanceof GroupStencil))
                .forEach(stencil -> groupMap.get(stencil.getParentGroup()).add(stencil));

        groupMap.values().stream().forEach(stencilsList -> stencilsList.sort((o1, o2) -> o1.getOrderNo() - o2.getOrderNo()));

        for (Map.Entry<Stencil, List<Stencil>> entry : groupMap.entrySet()) {
            Stencil groupStencil = entry.getKey();
            List<Stencil> stencilsInsideGroup = entry.getValue();
            stencilsDs.addItem(groupStencil);
            stencilsInsideGroup.stream().forEach(stencilsDs::addItem);
        }
    }

    protected class MoveStencilUpAction extends BaseAction {

        protected MoveStencilUpAction() {
            super("moveUp");
        }

        @Override
        public void actionPerform(Component component) {
            Stencil selectedStencil = stencilsDs.getItem();
            Integer currentOrderNo = selectedStencil.getOrderNo();
            stencilsDs.getItems().stream()
                    .filter(stencil -> stencil.getParentGroup() != null &&
                            stencil.getParentGroup().equals(selectedStencil.getParentGroup())
                            && stencil.getOrderNo() < currentOrderNo)
                    .max((o1, o2) -> o1.getOrderNo() - o2.getOrderNo())
                    .ifPresent(prevStencil -> {
                        selectedStencil.setOrderNo(prevStencil.getOrderNo());
                        prevStencil.setOrderNo(currentOrderNo);
                        refreshDs();
                        stencilsTable.setSelected(selectedStencil);
                    });
        }

        @Override
        protected boolean isApplicable() {
            Stencil selectedItem = stencilsDs.getItem();
            if (selectedItem != null && selectedItem instanceof ServiceTaskStencil) {
                for (Stencil stencil : stencilsDs.getItems()) {
                    if (selectedItem.getParentGroup().equals(stencil.getParentGroup())
                            && stencil.getOrderNo() < selectedItem.getOrderNo()) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String getCaption() {
            return "";
        }
    }

    protected class MoveStencilDownAction extends BaseAction {

        protected MoveStencilDownAction() {
            super("moveDown");
        }

        @Override
        public void actionPerform(Component component) {
            Stencil selectedStencil = stencilsDs.getItem();
            Integer currentOrderNo = selectedStencil.getOrderNo();
            stencilsDs.getItems().stream()
                    .filter(stencil -> stencil.getParentGroup() != null &&
                            stencil.getParentGroup().equals(selectedStencil.getParentGroup())
                            && stencil.getOrderNo() > currentOrderNo)
                    .min((o1, o2) -> o1.getOrderNo() - o2.getOrderNo())
                    .ifPresent(nextStencil -> {
                        selectedStencil.setOrderNo(nextStencil.getOrderNo());
                        nextStencil.setOrderNo(currentOrderNo);
                        refreshDs();
                        stencilsTable.setSelected(selectedStencil);
                    });
        }

        @Override
        protected boolean isApplicable() {
            Stencil selectedItem = stencilsDs.getItem();
            if (selectedItem != null && selectedItem instanceof ServiceTaskStencil) {
                for (Stencil stencil : stencilsDs.getItems()) {
                    if (selectedItem.getParentGroup().equals(stencil.getParentGroup())
                            && stencil.getOrderNo() > selectedItem.getOrderNo()) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String getCaption() {
            return "";
        }
    }
}
