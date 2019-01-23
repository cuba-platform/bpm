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
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcRole;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.security.entity.User;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A service that helps in working with BPM entities
 */
public interface BpmEntitiesService {
    String NAME = "bpm_BpmEntitiesService";

    @Nullable
    ProcDefinition findProcDefinitionByCode(String procDefinitionCode, String viewName);

    List<ProcInstance> findActiveProcInstancesForEntity(String procDefinitionCode, Entity entity, String viewName);

    List<ProcTask> findActiveProcTasks(ProcInstance procInstance, User user, String viewName);

    @Nullable
    ProcRole findProcRole(String procDefinitionCode, String procRoleCode, String viewName);

    List<ProcTask> findActiveProcTasksForCurrentUser(ProcInstance procInstance, String viewName);

    /**
     * Creates a new not-persisted ProcInstance according to the information from the {@code procInstanceDetails}
     */
    ProcInstance createProcInstance(ProcInstanceDetails procInstanceDetails);

    /**
     * The class is used for configuring a method that builds a {@link ProcInstance} entity containing process actors
     * and reference to the entity and to the {@link ProcInstance}.
     * <p>
     * Usage example:
     * <pre>
     * BpmEntitiesService.ProcInstance procInstance = new BpmEntitiesService.ProcInstanceDetails("someProcDefinitionCode")
     *      .setEntity(getItem())
     *      .addProcActor("manager", userSession.getCurrentOrSubstitutedUser())
     *      .addProcActor("storekeeper", someOtherUser);
     * </pre>
     */
    class ProcInstanceDetails implements Serializable {

        protected ProcDefinition procDefinition;

        protected String procDefinitionCode;

        protected List<ProcActorDetails> procActorDetails = new ArrayList<>();
        private Entity entity;


        public ProcInstanceDetails(String procDefinitionCode) {
            this.procDefinitionCode = procDefinitionCode;
        }


        public ProcInstanceDetails(ProcDefinition procDefinition) {
            this.procDefinition = procDefinition;
        }

        public ProcInstanceDetails addProcActor(String procRoleCode, User user) {
            this.procActorDetails.add(new ProcActorDetails(procRoleCode, user));
            return this;
        }

        public ProcInstanceDetails addProcActor(ProcRole procRole, User user) {
            this.procActorDetails.add(new ProcActorDetails(procRole, user));
            return this;
        }

        public ProcInstanceDetails setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }

        public ProcDefinition getProcDefinition() {
            return procDefinition;
        }

        public String getProcDefinitionCode() {
            return procDefinitionCode;
        }

        public List<ProcActorDetails> getProcActorDetails() {
            return procActorDetails;
        }

        public Entity getEntity() {
            return entity;
        }

        protected class ProcActorDetails implements Serializable {
            protected String procRoleCode;
            protected ProcRole procRole;
            protected User user;

            public ProcActorDetails(String procRoleCode, User user) {
                this.procRoleCode = procRoleCode;
                this.user = user;
            }

            public ProcActorDetails(ProcRole procRole, User user) {
                this.procRole = procRole;
                this.user = user;
            }

            public String getProcRoleCode() {
                return procRoleCode;
            }

            public User getUser() {
                return user;
            }

            public ProcRole getProcRole() {
                return procRole;
            }
        }
    }
}
