/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.procactor;

import com.haulmont.bpm.entity.ProcActor;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcRole;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.User;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProcActorsFrame extends AbstractFrame {

    @Inject
    protected Datasource<ProcInstance> procInstanceDs;

    @Inject
    protected CollectionDatasource.Ordered<ProcActor, UUID> procActorsDs;

    @Inject
    protected CollectionDatasource<ProcRole, UUID> procRolesDs;

    @Inject
    protected LookupField procRolesLookup;

    @Inject
    protected Metadata metadata;

    protected ProcInstance procInstance;

    @Inject
    protected Table<ProcActor> procActorsTable;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected CollectionDatasource<User, UUID> usersDs;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initProcRolesLookup();
        initProcActorsTable();
    }

    protected void initProcActorsTable() {
        procActorsTable.addGeneratedColumn("user", entity -> {
            LookupField lookupField = componentsFactory.createComponent(LookupField.class);
            lookupField.setWidth("100%");
            lookupField.setOptionsDatasource(usersDs);
            lookupField.setValue(entity.getUser());
            lookupField.addValueChangeListener(e -> {
                entity.setUser((User) e.getValue());
            });
            return lookupField;
        });
    }

    protected void initProcRolesLookup() {
        procRolesLookup.addValueChangeListener(e -> {
            if (e.getValue() == null) {
                return;
            }
            addProcActor((ProcRole) e.getValue());
            procRolesLookup.setValue(null);
        });
    }

    protected void addProcActor(ProcRole procRole) {
        ProcActor procActor = metadata.create(ProcActor.class);
        procActor.setProcRole(procRole);
        procActor.setProcInstance(procInstance);
        procActor.setOrder(getLastOrder(procRole) + 1);
        procActorsDs.addItem(procActor);
    }

    protected int getLastOrder(ProcRole procRole) {
        int lastOrder = 0;
        for (ProcActor procActor : procActorsDs.getItems()) {
            if (procRole.equals(procActor.getProcRole()) && procActor.getOrder() > lastOrder) {
                lastOrder = procActor.getOrder();
            }
        }
        return lastOrder;
    }

    public void commit() {
        procActorsDs.commit();
    }

    public void setProcInstance(ProcInstance procInstance) {
        this.procInstance = procInstance;
        procInstanceDs.setItem(procInstance);
        refreshProcRolesDs();
    }

    public void refreshProcRolesDs() {
        Map<String, Object> params = new HashMap<>();
        params.put("procDefinition", procInstance.getProcDefinition());
        procRolesDs.refresh(params);
    }

    public void moveUp() {
        ProcActor procActor = procActorsDs.getItem();
        if (procActor == null || procActorsDs.isFirstId(procActor.getId())) return;

        UUID prevItemId = procActorsDs.prevItemId(procActor.getId());
        ProcActor prevProcActor = procActorsDs.getItem(prevItemId);

        if (!procActor.getProcRole().equals(prevProcActor.getProcRole())) return;

        Integer tmp = prevProcActor.getOrder();
        prevProcActor.setOrder(procActor.getOrder());
        procActor.setOrder(tmp);

        sortProcActors();
    }

    public void moveDown() {
        ProcActor procActor = procActorsDs.getItem();
        if (procActor == null || procActorsDs.isLastId(procActor.getId())) return;

        UUID nextItemId = procActorsDs.nextItemId(procActor.getId());
        ProcActor nextProcActor = procActorsDs.getItem(nextItemId);

        if (!procActor.getProcRole().equals(nextProcActor.getProcRole())) return;

        Integer tmp = nextProcActor.getOrder();
        nextProcActor.setOrder(procActor.getOrder());
        procActor.setOrder(tmp);

        sortProcActors();
    }

    protected void sortProcActors() {
        CollectionDatasource.Sortable.SortInfo procRoleOrderSortInfo = new CollectionDatasource.Sortable.SortInfo();
        procRoleOrderSortInfo.setOrder(CollectionDatasource.Sortable.Order.ASC);
        procRoleOrderSortInfo.setPropertyPath(procActorsDs.getMetaClass().getPropertyPath("procRole.order"));

        CollectionDatasource.Sortable.SortInfo procActorOrderSortInfo = new CollectionDatasource.Sortable.SortInfo();
        procActorOrderSortInfo.setOrder(CollectionDatasource.Sortable.Order.ASC);
        procActorOrderSortInfo.setPropertyPath(procActorsDs.getMetaClass().getPropertyPath("order"));

        ((CollectionDatasource.Sortable) procActorsDs).sort(new CollectionDatasource.Sortable.SortInfo[]{procRoleOrderSortInfo, procActorOrderSortInfo});
    }
}