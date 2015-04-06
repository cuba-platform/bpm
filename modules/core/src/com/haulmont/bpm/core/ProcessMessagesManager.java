/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
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

    String getMessage(String actProcessDefinitionIdn, String key);

    String getMessage(String actProcessDefinitionId, String key, Locale locale);
}
