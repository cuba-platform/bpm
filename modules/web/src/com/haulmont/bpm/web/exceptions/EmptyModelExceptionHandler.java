/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.web.exceptions;

import com.haulmont.bpm.exception.InvalidModelException;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.exception.AbstractGenericExceptionHandler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("bpm_EmptyModelExceptionHandler")
public class EmptyModelExceptionHandler extends AbstractGenericExceptionHandler {

    @Inject
    protected Messages messages;

    public EmptyModelExceptionHandler() {
        super(InvalidModelException.class.getName());
    }

    @Override
    protected void doHandle(String className, String message, Throwable throwable, WindowManager windowManager) {
        String msg = message;
        if ("Model is empty".equals(message)) {
            msg = messages.getMessage(EmptyModelExceptionHandler.class, "emptyModel.msg");
        } else if ("Model elements are not linked properly".equals(message)) {
            msg = messages.getMessage(EmptyModelExceptionHandler.class, "missingLinking.msg");
        }
        windowManager.showNotification(msg, Frame.NotificationType.WARNING);
    }
}