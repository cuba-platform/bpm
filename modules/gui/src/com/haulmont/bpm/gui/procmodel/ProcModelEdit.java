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

import com.haulmont.bpm.rest.RestModel;
import com.haulmont.bpm.service.ModelService;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.bpm.entity.ProcModel;

import javax.inject.Inject;
import java.util.Map;

/**
 * Model editor is also used for copying a model. In this case a {@code srcModel} window parameter
 * will be passed.
 */
public class ProcModelEdit extends AbstractEditor<ProcModel> {

    @Inject
    protected ModelService modelService;

    @WindowParam(name = "srcModel")
    protected ProcModel srcModel;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        getDialogOptions().setWidthAuto();
    }

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