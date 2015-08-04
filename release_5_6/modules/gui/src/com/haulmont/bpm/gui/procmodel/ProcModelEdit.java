/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.procmodel;

import com.haulmont.bpm.rest.RestModel;
import com.haulmont.bpm.service.ModelService;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.bpm.entity.ProcModel;

import javax.inject.Inject;

/**
 * Model editor is also used for copying a model. In this case a {@code srcModel} window parameter
 * will be passed.
 * @author gorbunkov
 */
public class ProcModelEdit extends AbstractEditor<ProcModel> {

    @Inject
    protected ModelService modelService;

    @WindowParam(name = "srcModel")
    protected ProcModel srcModel;

    @Override
    protected boolean preCommit() {
        if (srcModel != null) {
            copyModel();
        } else if (PersistenceHelper.isNew(getItem())) {
            String actModelId = modelService.createModel(getItem().getName());
            getItem().setActModelId(actModelId);
        }
        return true;
    }

    protected void copyModel() {
        ProcModel model = getItem();
        String actModelId = modelService.createModel(getItem().getName());
        model.setActModelId(actModelId);
        RestModel modelJson = modelService.getModelJson(srcModel.getActModelId());
        String modifiedJson = modelService.updateModelNameInJson(modelJson.getModelJson(), model.getName());
        modelService.updateModel(actModelId, model.getName(), model.getDescription(), modifiedJson, null);
    }

    @Override
    protected boolean postCommit(boolean committed, boolean close) {
        if (committed) {
            modelService.updateModel(getItem().getActModelId(), getItem().getName(), getItem().getDescription());
        }
        return super.postCommit(committed, close);
    }
}