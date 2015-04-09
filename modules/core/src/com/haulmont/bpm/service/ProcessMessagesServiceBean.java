/*
 * Copyright (c) 2015 com.haulmont.bpm.service
 */
package com.haulmont.bpm.service;

import com.haulmont.bpm.core.ProcessMessagesManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Locale;

/**
 * @author gorbunkov
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
    public String loadString(String actProcessDefinitionId, String key, Locale locale) {
        return processMessagesManager.loadString(actProcessDefinitionId, key, locale);
    }

    @Override
    public String loadString(String actProcessDefinitionId, String key) {
        return processMessagesManager.loadString(actProcessDefinitionId, key);
    }
}