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

package com.haulmont.bpm.gui.stencilset.frame;

import com.haulmont.bpm.entity.stencil.Stencil;
import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.data.Datasource;

import javax.inject.Inject;

public class AbstractStencilFrame<T extends Stencil> extends AbstractFrame {

    @Inject
    protected Datasource<T> stencilDs;

    public void setStencil(T stencil) {
        stencilDs.setItem(stencil);
    }
}
