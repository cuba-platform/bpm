/*
 * Copyright (c) 115 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */
package com.haulmont.bpm.gui.procmodel;

import com.haulmont.bpm.service.ModelService;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.bpm.entity.ProcModel;

import javax.inject.Inject;

/**
 * @author gorbunkov
 */
public class ProcModelEdit extends AbstractEditor<ProcModel> {

    @Inject
    protected ModelService modelService;

    @Override
    protected boolean preCommit() {
        if (PersistenceHelper.isNew(getItem())) {
            String actModelId = modelService.createModel(getItem().getName());
            getItem().setActModelId(actModelId);
        }
        return true;
    }
}