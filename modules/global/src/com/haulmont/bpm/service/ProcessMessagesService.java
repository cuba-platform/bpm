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

package com.haulmont.bpm.service;

import com.haulmont.bpm.entity.ProcDefinition;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Class provides a way to work process messages from a client tier. For documentation see {@code ProcessMessagesManager}
 *
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