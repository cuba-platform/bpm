/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * @author gorbunkov
 * @version $Id$
 */
public interface ProcessMessagesManager {

    String NAME = "bpm_ProcessMessagesManager";

    String getMessage(String actProcessDefinitionId, String key);

    String getMessage(String actProcessDefinitionId, String key, Locale locale);

    @Nullable
    String findMessage(String actProcessDefinitionId, String key);

    @Nullable
    String findMessage(String actProcessDefinitionId, String key, Locale locale);

    String loadString(String actProcessDefinitionId, String key);

    String loadString(String actProcessDefinitionId, String key, Locale locale);

    void clearCaches();
}
