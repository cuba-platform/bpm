/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.cuba.core.app.AbstractBeansMetadata;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Class that is used by Stencils editor for getting an information about middleware beans names and their methods
 */
@Component("bpm_StencilsBeansMetadata")
public class StencilsBeansMetadata extends AbstractBeansMetadata {

    @Override
    protected boolean isMethodAvailable(Method method) {
        return true;
    }
}
