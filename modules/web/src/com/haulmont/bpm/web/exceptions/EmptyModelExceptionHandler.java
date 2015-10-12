/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.web.exceptions;

import com.haulmont.bpm.exception.EmptyModelException;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.exception.AbstractGenericExceptionHandler;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

/**
 * @author gorbunkov
 * @version $Id$
 */
@ManagedBean("bpm_EmptyModelExceptionHandler")
public class EmptyModelExceptionHandler extends AbstractGenericExceptionHandler {

    @Inject
    protected Messages messages;

    public EmptyModelExceptionHandler() {
        super(EmptyModelException.class.getName());
    }

    @Override
    protected void doHandle(String className, String message, Throwable throwable, WindowManager windowManager) {
        String msg = messages.getMessage(EmptyModelExceptionHandler.class, "emptyModel.msg");
        windowManager.showNotification(msg, Frame.NotificationType.WARNING);
    }
}
