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

package com.haulmont.bpm;

public interface BpmConstants {
    String DEFAULT_TASK_OUTCOME = "defaultComplete";
    String STANDARD_PROC_FORM = "standardProcForm";
    String CUSTOM_STENCIL_SERVICE_TASK = "serviceTask";

    interface Views {
        String PROC_INSTANCE_FULL = "procInstance-full";
        String PROC_TASK_COMPLETE = "procTask-complete";
        String PROC_DEFINITION_WITH_ROLES = "procDefinition-withRoles";
    }
}