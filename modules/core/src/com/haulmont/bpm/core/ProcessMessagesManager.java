/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.core;

import com.haulmont.bpm.entity.ProcDefinition;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Class is used for working with localization messages defined in process
 * @author gorbunkov
 * @version $Id$
 */
public interface ProcessMessagesManager {

    String NAME = "bpm_ProcessMessagesManager";

    /**
     * Returns localized message from BPMN process.<br/>
     * Locale is determined by the current user session.
     * @param actProcessDefinitionId activiti process definition id
     * @param key message key
     * @return localized message or the key if the message not found
     */
    String getMessage(String actProcessDefinitionId, String key);

    /**
     * Returns localized message from BPMN process.
     * @param actProcessDefinitionId activiti process definition id
     * @param key message key
     * @param locale message locale
     * @return localized message or the key if the message not found
     */
    String getMessage(String actProcessDefinitionId, String key, Locale locale);

    /**
     * Returns localized message from BPMN process or null if not found.<br/>
     * Locale is determined by the current user session.
     * @param actProcessDefinitionId activiti process definition id
     * @param key message key
     * @return localized message or the key if the message not found
     */
    @Nullable
    String findMessage(String actProcessDefinitionId, String key);

    /**
     * Returns localized message from BPMN process or null if not found.
     * @param actProcessDefinitionId activiti process definition id
     * @param key message key
     * @param locale message locale
     * @return localized message or the key if the message not found
     */
    @Nullable
    String findMessage(String actProcessDefinitionId, String key, Locale locale);

    /**
     * Get localized message from a process by reference provided in the full format.<br/>
     * Locale is determined by the current user session.
     * @param actProcessDefinitionId activiti process definition id
     * @param ref   reference to message in the following format: <code>msg://message_key</code>
     * @return      localized message or input string itself if it doesn't begin with <code>msg://</code>
     */
    String loadString(String actProcessDefinitionId, String ref);

    /**
     * Get localized message from a process by reference provided in the full format.<br/>
     * @param actProcessDefinitionId activiti process definition id
     * @param ref   reference to message in the following format: <code>msg://message_key</code>
     * @param locale message locale
     * @return      localized message or input string itself if it doesn't begin with <code>msg://</code>
     */
    String loadString(String actProcessDefinitionId, String ref, Locale locale);

    /**
     * Clears messages caches
     */
    void clearCaches();
}
