/*
 * Copyright (c) 2015 com.haulmont.bpm.entity
 */
package com.haulmont.bpm.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.security.entity.User;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.openjpa.persistence.Persistent;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.global.DeletePolicy;
import java.util.Set;
import javax.persistence.OneToMany;
import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.NamePattern;
import javax.persistence.Lob;

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

    @Persistent
    @Column(name = "ENTITY_ID")
    protected UUID entityId;

    @Column(name = "ACTIVE")
    protected Boolean active;

    @Column(name = "CANCELED")
    protected Boolean canceled;

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


    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }

    public Boolean getCanceled() {
        return canceled;
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