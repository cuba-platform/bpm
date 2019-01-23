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
        if (procInstance != null) {
            params.put("procInstance", procInstance.getId());
        }
        procTasksDs.refresh(params);
    }
}