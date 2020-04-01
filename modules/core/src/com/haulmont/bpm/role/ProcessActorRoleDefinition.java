/*
 * Copyright (c) 2008-2020 Haulmont.
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

package com.haulmont.bpm.role;

import com.haulmont.bpm.entity.*;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.security.app.role.AnnotatedRoleDefinition;
import com.haulmont.cuba.security.app.role.annotation.EntityAccess;
import com.haulmont.cuba.security.app.role.annotation.EntityAttributeAccess;
import com.haulmont.cuba.security.app.role.annotation.Role;
import com.haulmont.cuba.security.app.role.annotation.ScreenAccess;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.role.EntityAttributePermissionsContainer;
import com.haulmont.cuba.security.role.EntityPermissionsContainer;
import com.haulmont.cuba.security.role.ScreenPermissionsContainer;

/**
 * Predefined role definition allows to participate as a process actor. The role grants access to process related
 * entities. Screen from the BPM menu (e.g. Process Tasks) are not available by default. If the user must access them,
 * then the access must be granted by another role.
 */
@Role(name = ProcessActorRoleDefinition.NAME)
public class ProcessActorRoleDefinition extends AnnotatedRoleDefinition {

    public static final String NAME = "bpm-process-actor";

    @Override
    @ScreenAccess(screenIds = {
            "standardProcForm"
    })
    public ScreenPermissionsContainer screenPermissions() {
        return super.screenPermissions();
    }

    @Override
    @EntityAccess(entityClass = ProcDefinition.class,
            operations = {EntityOp.READ})
    @EntityAccess(entityClass = ProcInstance.class,
            operations = {EntityOp.CREATE, EntityOp.READ, EntityOp.UPDATE})
    @EntityAccess(entityClass = ProcTask.class,
            operations = {EntityOp.CREATE, EntityOp.READ, EntityOp.UPDATE})
    @EntityAccess(entityClass = ProcRole.class,
            operations = {EntityOp.READ, EntityOp.UPDATE})
    @EntityAccess(entityClass = ProcActor.class,
            operations = {EntityOp.CREATE, EntityOp.READ, EntityOp.UPDATE, EntityOp.DELETE})
    @EntityAccess(entityClass = ProcAttachment.class,
            operations = {EntityOp.CREATE, EntityOp.READ, EntityOp.UPDATE, EntityOp.DELETE})
    @EntityAccess(entityClass = ProcAttachmentType.class,
            operations = {EntityOp.READ})
    @EntityAccess(entityClass = FileDescriptor.class,
            operations = {EntityOp.CREATE, EntityOp.READ, EntityOp.UPDATE})
    @EntityAccess(entityClass = User.class, operations = {EntityOp.READ})
    public EntityPermissionsContainer entityPermissions() {
        return super.entityPermissions();
    }

    @Override
    @EntityAttributeAccess(entityClass = ProcDefinition.class, view = "*")
    @EntityAttributeAccess(entityClass = ProcInstance.class, modify = "*")
    @EntityAttributeAccess(entityClass = ProcTask.class, modify = "*")
    @EntityAttributeAccess(entityClass = ProcRole.class, view = "*")
    @EntityAttributeAccess(entityClass = ProcActor.class, modify = "*")
    @EntityAttributeAccess(entityClass = ProcAttachment.class, modify = "*")
    @EntityAttributeAccess(entityClass = ProcAttachmentType.class, view = "*")
    @EntityAttributeAccess(entityClass = User.class, view = "*")
    @EntityAttributeAccess(entityClass = FileDescriptor.class, modify = "*")
    public EntityAttributePermissionsContainer entityAttributePermissions() {
        return super.entityAttributePermissions();
    }

    @Override
    public String getLocName() {
        return "BPM process actor";
    }

}
