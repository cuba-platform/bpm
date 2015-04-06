/*
 * Copyright (c) 2015 com.haulmont.bpm.entity
 */
package com.haulmont.bpm.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.common.base.Strings;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import javax.persistence.Lob;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.security.entity.User;
import java.util.Set;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

/**
 * @author gorbunkov
 */
@Table(name = "BPM_PROC_TASK")
@Entity(name = "bpm$ProcTask")
public class ProcTask extends StandardEntity {

    private static final long serialVersionUID = -3420632200646053615L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROC_INSTANCE_ID")
    protected ProcInstance procInstance;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "START_DATE")
    protected Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "END_DATE")
    protected Date endDate;

    @Column(name = "OUTCOME")
    protected String outcome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ACTOR_ID")
    protected ProcActor procActor;

    @Column(name = "ACT_EXECUTION_ID", nullable = false)
    protected String actExecutionId;

    @Column(name = "NAME")
    protected String name;

    @Column(name = "ACT_TASK_ID")
    protected String actTaskId;

    @Lob
    @Column(name = "COMMENT_")
    protected String comment;

    @Column(name = "CANCELED")
    protected Boolean canceled;

    @JoinTable(name = "BPM_PROC_TASK_USER_LINK",
        joinColumns = @JoinColumn(name = "PROC_TASK_ID"),
        inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    @ManyToMany
    protected Set<User> candidateUsers;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CLAIM_DATE")
    protected Date claimDate;

    @Column(name = "ACT_PROCESS_DEFINITION_ID")
    protected String actProcessDefinitionId;

    @MetaProperty
    public String getLocName() {
        if (!Strings.isNullOrEmpty(actProcessDefinitionId) && !Strings.isNullOrEmpty(name)) {
            ProcessMessagesService processMessagesService = AppBeans.get(ProcessMessagesService.class);
            return processMessagesService.getMessage(actProcessDefinitionId, name);
        }
        return name;
    }

    @MetaProperty
    public String getLocOutcome() {
        if (!Strings.isNullOrEmpty(actProcessDefinitionId) && !Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(outcome)) {
            ProcessMessagesService processMessagesService = AppBeans.get(ProcessMessagesService.class);
            return processMessagesService.getMessage(actProcessDefinitionId, name + "." + outcome);
        }
        return outcome;
    }

    public void setActProcessDefinitionId(String actProcessDefinitionId) {
        this.actProcessDefinitionId = actProcessDefinitionId;
    }

    public String getActProcessDefinitionId() {
        return actProcessDefinitionId;
    }


    public void setClaimDate(Date claimDate) {
        this.claimDate = claimDate;
    }

    public Date getClaimDate() {
        return claimDate;
    }




    public void setCandidateUsers(Set<User> candidateUsers) {
        this.candidateUsers = candidateUsers;
    }

    public Set<User> getCandidateUsers() {
        return candidateUsers;
    }


    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }

    public Boolean getCanceled() {
        return canceled;
    }


    public void setActExecutionId(String actExecutionId) {
        this.actExecutionId = actExecutionId;
    }

    public String getActExecutionId() {
        return actExecutionId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setActTaskId(String actTaskId) {
        this.actTaskId = actTaskId;
    }

    public String getActTaskId() {
        return actTaskId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setProcInstance(ProcInstance procInstance) {
        this.procInstance = procInstance;
    }

    public ProcInstance getProcInstance() {
        return procInstance;
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

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setProcActor(ProcActor procActor) {
        this.procActor = procActor;
    }

    public ProcActor getProcActor() {
        return procActor;
    }


}