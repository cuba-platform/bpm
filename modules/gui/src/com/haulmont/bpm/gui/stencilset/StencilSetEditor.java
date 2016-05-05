/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.stencilset;

import com.haulmont.bpm.entity.stencil.GroupStencil;
import com.haulmont.bpm.entity.stencil.ServiceTaskStencil;
import com.haulmont.bpm.entity.stencil.StandardStencil;
import com.haulmont.bpm.entity.stencil.Stencil;
import com.haulmont.bpm.gui.stencilset.frame.AbstractStencilFrame;
import com.haulmont.bpm.gui.stencilset.frame.ServiceTaskStencilFrame;
import com.haulmont.bpm.gui.stencilset.helper.StencilSetJsonHelper;
import com.haulmont.bpm.service.StencilSetService;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.*;

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

    @Named("stencilsTable.remove")
    protected RemoveAction stencilsTableRemove;

    @Inject
    protected StencilSetService stencilSetService;

    protected AbstractStencilFrame activeStencilFrame;

    protected boolean treeItemChangeListenerEnabled = true;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        initData();

        stencilsDs.addItemChangeListener(e -> {
            if (!treeItemChangeListenerEnabled) return;

            Stencil item = e.getItem();

            if (activeStencilFrame != null) {
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

        stencilsTableRemove.setAutocommit(false);
    }

    protected void initData() {
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
        ServiceTaskStencil stencilEntity = metadata.create(ServiceTaskStencil.class);
        stencilEntity.setParentGroup(group);
        stencilsDs.addItem(stencilEntity);
        stencilsTable.setSelected(stencilEntity);
        activeStencilFrame.requestFocus();
        stencilsTable.expand(group.getId());
    }

    public void saveStencilSet() throws IOException {
        String modifiedStencilSetJson = StencilSetJsonHelper.generateCustomStencilSet(stencilsDs.getItems());
        stencilSetService.setStencilSet(modifiedStencilSetJson);
        stencilsDs.getItems().stream()
                .filter(stencil -> stencil instanceof ServiceTaskStencil)
                .forEach(stencil -> stencilSetService.registerServiceTaskStencilBpmnJsonConverter(stencil.getStencilId()));
        close(COMMIT_ACTION_ID, true);
    }

    public void cancel() {
        close(CLOSE_ACTION_ID, true);
    }

    public void resetStencilSet() {
        showOptionDialog(getMessage("resetStencilSetDlg.caption"),
                getMessage("resetStencilSetDlg.message"),
                MessageType.CONFIRMATION,
                new Action[] {
                        new DialogAction(DialogAction.Type.YES) {
                            @Override
                            public void actionPerform(Component component) {
                                groupsDs.clear();
                                stencilsDs.clear();
                                stencilSetService.resetStencilSet();
                                initData();
                            }
                        },
                        new DialogAction(DialogAction.Type.NO) {
                            @Override
                            public void actionPerform(Component component) {}
                        }
                });
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
}
