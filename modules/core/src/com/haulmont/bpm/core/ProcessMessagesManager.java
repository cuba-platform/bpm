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

package com.haulmont.bpm.core;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Class is used for working with localization messages defined in process
 */
public interface ProcessMessagesManager {

    String NAME = "bpm_ProcessMessagesManager";

    /**
     * Returns localized message from BPMN process.<br>
     * Locale is determined by the current user session.
     *
     * @param actProcessDefinitionId activiti process definition id
     * @param key                    message key
     * @return localized message or the key if the message not found
     */
    String getMessage(String actProcessDefinitionId, String key);

    /**
     * Returns localized message from BPMN process.
     *
     * @param actProcessDefinitionId activiti process definition id
     * @param key                    message key
     * @param locale                 message locale
     * @return localized message or the key if the message not found
     */
    String getMessage(String actProcessDefinitionId, String key, Locale locale);

    /**
     * Returns localized message from BPMN process or null if not found.<br>
     * Locale is determined by the current user session.
     *
     * @param actProcessDefinitionId activiti process definition id
     * @param key                    message key
     * @return localized message or the key if the message not found
     */
    @Nullable
    String findMessage(String actProcessDefinitionId, String key);

    /**
     * Returns localized message from BPMN process or null if not found.
     *
     * @param actProcessDefinitionId activiti process definition id
     * @param key                    message key
     * @param locale                 message locale
     * @return localized message or the key if the message not found
     */
    @Nullable
    String findMessage(String actProcessDefinitionId, String key, Locale locale);

    /**
     * Get localized message from a process by reference provided in the full format.<br>
     * Locale is determined by the current user session.
     *
     * @param actProcessDefinitionId activiti process definition id
     * @param ref                    reference to message in the following format: {@code msg://message_key}
     * @return localized message or input string itself if it doesn't begin with {@code msg://}
     */
    String loadString(String actProcessDefinitionId, String ref);

    /**
     * Get localized message from a process by reference provided in the full format.<br>
     *
     * @param actProcessDefinitionId activiti process definition id
     * @param ref                    reference to message in the following format: {@code msg://message_key}
     * @param locale                 message locale
     * @return localized message or input string itself if it doesn't begin with {@code msg://}
     */
    String loadString(String actProcessDefinitionId, String ref, Locale locale);

    /**
     * Clears messages caches
     */
    void clearCaches();
}