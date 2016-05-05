/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.stencilset.frame;

import com.haulmont.bpm.entity.stencil.GroupStencil;
import com.haulmont.cuba.gui.components.FieldGroup;

import javax.inject.Inject;

public class GroupStencilFrame extends AbstractStencilFrame<GroupStencil> {

    @Inject
    private FieldGroup fieldGroup;

    @Override
    public void setStencil(GroupStencil stencil) {
        super.setStencil(stencil);
        fieldGroup.setEditable(stencil.getEditable());
    }

    @Override
    public void requestFocus() {
        fieldGroup.requestFocus();
    }
}
