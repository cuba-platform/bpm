/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcDefinition;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Class provides a way to work process messages from a client tier. For documentation see {@code ProcessMessagesManager}
 *
 * @author gorbunkov
 * @version $Id$
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