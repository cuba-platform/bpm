/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.core.ProcessMessagesManager;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Locale;

/**
 */
@Service(ProcessMessagesService.NAME)
public class ProcessMessagesServiceBean implements ProcessMessagesService {
    @Inject
    protected ProcessMessagesManager processMessagesManager;

    @Override
    public String getMessage(String actProcessDefinitionId, String key) {
        return processMessagesManager.getMessage(actProcessDefinitionId, key);
    }

    @Override
    public String getMessage(String actProcessDefinitionId, String key, Locale locale) {
        return processMessagesManager.getMessage(actProcessDefinitionId, key, locale);
    }

    @Override
    @Nullable
    public String findMessage(String actProcessDefinitionId, String key) {
        return processMessagesManager.findMessage(actProcessDefinitionId, key);
    }

    @Override
    @Nullable
    public String findMessage(String actProcessDefinitionId, String key, Locale locale) {
        return processMessagesManager.findMessage(actProcessDefinitionId, key, locale);
    }

    @Override
    public String loadString(String actProcessDefinitionId, String key, Locale locale) {
        return processMessagesManager.loadString(actProcessDefinitionId, key, locale);
    }

    @Override
    public String loadString(String actProcessDefinitionId, String key) {
        return processMessagesManager.loadString(actProcessDefinitionId, key);
    }
}