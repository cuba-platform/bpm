/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.entity;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.global.DeletePolicy;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * @author gorbunkov
 */
@NamePattern("%s (%s)|procDefinition,id")
@Table(name = "BPM_PROC_INSTANCE")
@Entity(name = "bpm$ProcInstance")
public class ProcInstance extends StandardEntity {

    private static final long serialVersionUID = -4194857660249569575L;

    @Column(name = "ENTITY_NAME")
    protected String entityName;

    @Column(name = "ENTITY_ID")
    protected UUID entityId;

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

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public UUID getEntityId() {
        return entityId;
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