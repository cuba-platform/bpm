/*
 * Copyright (c) 2015 com.haulmont.bpm.service
 */
package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcDefinition;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * @author gorbunkov
 */
public interface ProcessMessagesService {
    String NAME = "bpm_ProcessMessagesService";

    String getMessage(String actProcessdefinitionId, String key);

    String getMessage(String actProcessDefinitionId, String key, Locale locale);

    String loadString(String actProcessDefinitionId, String key);

    @Nullable
    String findMessage(String actProcessDefinitionId, String key);

    @Nullable
    String findMessage(String actProcessDefinitionId, String key, Locale locale);

    String loadString(String actProcessDefinitionId, String key, Locale locale);
}