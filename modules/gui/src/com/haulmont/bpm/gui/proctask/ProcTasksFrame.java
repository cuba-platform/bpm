/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.proctask;

import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.data.CollectionDatasource;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProcTasksFrame extends AbstractFrame {

    protected ProcInstance procInstance;

    @Inject
    protected CollectionDatasource<ProcTask, UUID> procTasksDs;

    public void setProcInstance(ProcInstance procInstance) {
        this.procInstance = procInstance;
    }

    public void refresh() {
        Map<String, Object> params = new HashMap<>();
        if (procInstance != null)
            params.put("procInstance", procInstance);
        procTasksDs.refresh(params);
    }
}