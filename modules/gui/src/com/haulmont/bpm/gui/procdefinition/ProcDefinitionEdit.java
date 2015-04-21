/*
 * Copyright (c) 2015 com.haulmont.bpm.gui.procdefinition
 */
package com.haulmont.bpm.gui.procdefinition;

import com.haulmont.bpm.entity.ProcRole;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.cuba.gui.data.CollectionDatasource;

import javax.inject.Inject;
import java.util.UUID;

/**
 * @author gorbunkov
 */
public class ProcDefinitionEdit extends AbstractEditor<ProcDefinition> {

    @Inject
    private CollectionDatasource.Ordered<ProcRole, UUID> procRolesDs;

    @Inject
    private Metadata metadata;

    public void addProcRole() {
        ProcRole procRole = metadata.create(ProcRole.class);
        procRole.setProcDefinition(getItem());
        procRolesDs.addItem(procRole);
    }

    public void moveProcRoleUp() {
        ProcRole procRole = procRolesDs.getItem();
        if (procRole == null || procRolesDs.isFirstId(procRole.getId())) return;

        UUID prevItemId = procRolesDs.prevItemId(procRole.getId());
        ProcRole prevProcRole = procRolesDs.getItem(prevItemId);

        Integer tmp = prevProcRole.getOrder();
        prevProcRole.setOrder(procRole.getOrder());
        procRole.setOrder(tmp);

        sortProcRoles();
    }

    public void moveProcRoleDown() {
        ProcRole procRole = procRolesDs.getItem();
        if (procRole == null || procRolesDs.isLastId(procRole.getId())) return;

        UUID nextItemId = procRolesDs.nextItemId(procRole.getId());
        ProcRole nextProcRole = procRolesDs.getItem(nextItemId);

        Integer tmp = nextProcRole.getOrder();
        nextProcRole.setOrder(procRole.getOrder());
        procRole.setOrder(tmp);

        sortProcRoles();
    }

    protected void sortProcRoles() {
        CollectionDatasource.Sortable.SortInfo sortInfo = new CollectionDatasource.Sortable.SortInfo();
        sortInfo.setOrder(CollectionDatasource.Sortable.Order.ASC);
        sortInfo.setPropertyPath(procRolesDs.getMetaClass().getPropertyPath("order"));
        ((CollectionDatasource.Sortable) procRolesDs).sort(new CollectionDatasource.Sortable.SortInfo[] {sortInfo});
        procRolesDs.refresh();
    }
}