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