/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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
