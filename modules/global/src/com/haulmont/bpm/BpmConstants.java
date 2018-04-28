/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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