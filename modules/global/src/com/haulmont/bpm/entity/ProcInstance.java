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

package com.haulmont.bpm.entity;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.ReferenceToEntity;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DeletePolicy;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.entity.User;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@NamePattern("%s (%s)|procDefinition,id")
@Table(name = "BPM_PROC_INSTANCE")
@Entity(name = "bpm$ProcInstance")
public class ProcInstance extends StandardEntity {

    private static final long serialVersionUID = -4194857660249569575L;

    @Column(name = "ENTITY_NAME")
    protected String entityName;

    @Embedded
    protected ReferenceToEntity entity;

    @Column(name = "ACTIVE")
    protected Boolean active = false;

    @Column(name = "CANCELLED")
    protected Boolean cancelled = false;

    @Column(name = "ACT_PROCESS_INSTANCE_ID")
    protected String actProcessInstanceId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "START_DATE")
    protected Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "END_DATE")
    protected Date endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROC_DEFINITION_ID")
    protected ProcDefinition procDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STARTED_BY_ID")
    protected User startedBy;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "procInstance")
    protected Set<ProcTask> procTasks;

    @Lob
    @Column(name = "START_COMMENT")
    protected String startComment;

    @Lob
    @Column(name = "CANCEL_COMMENT")
    protected String cancelComment;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "procInstance")
    protected Set<ProcActor> procActors;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "procInstance")
    protected Set<ProcAttachment> procAttachments;


    @Column(name = "ENTITY_EDITOR_NAME")
    protected String entityEditorName;

    @Lob
    @Column(name = "DESCRIPTION")
    protected String description;

    @PostConstruct
    public void init() {
        Metadata metadata = AppBeans.get(Metadata.NAME);
        entity = metadata.create(ReferenceToEntity.class);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setEntityEditorName(String entityEditorName) {
        this.entityEditorName = entityEditorName;
    }

    public String getEntityEditorName() {
        return entityEditorName;
    }

    public void setProcAttachments(Set<ProcAttachment> procAttachments) {
        this.procAttachments = procAttachments;
    }

    public Set<ProcAttachment> getProcAttachments() {
        return procAttachments;
    }

    public void setStartComment(String startComment) {
        this.startComment = startComment;
    }

    public String getStartComment() {
        return startComment;
    }

    public void setCancelComment(String cancelComment) {
        this.cancelComment = cancelComment;
    }

    public String getCancelComment() {
        return cancelComment;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public void setActProcessInstanceId(String actProcessInstanceId) {
        this.actProcessInstanceId = actProcessInstanceId;
    }

    public String getActProcessInstanceId() {
        return actProcessInstanceId;
    }

    public void setProcTasks(Set<ProcTask> procTasks) {
        this.procTasks = procTasks;
    }

    public Set<ProcTask> getProcTasks() {
        return procTasks;
    }

    public void setProcActors(Set<ProcActor> procActors) {
        this.procActors = procActors;
    }

    public Set<ProcActor> getProcActors() {
        return procActors;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    public ReferenceToEntity getEntity() {
        return entity;
    }

    public void setEntity(ReferenceToEntity entity) {
        this.entity = entity;
    }

    public Object getObjectEntityId() {
        return entity != null ? entity.getObjectEntityId() : null;
    }

    public void setObjectEntityId(Object entityId) {
        if (entity != null) {
            entity.setObjectEntityId(entityId);
        }
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setProcDefinition(ProcDefinition procDefinition) {
        this.procDefinition = procDefinition;
    }

    public ProcDefinition getProcDefinition() {
        return procDefinition;
    }

    public void setStartedBy(User startedBy) {
        this.startedBy = startedBy;
    }

    public User getStartedBy() {
        return startedBy;
    }
}