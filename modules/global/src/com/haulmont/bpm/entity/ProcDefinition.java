/*
 * Copyright (c) 2015 com.haulmont.bpmn.entity
 */
package com.haulmont.bpm.entity;

import javax.persistence.*;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.global.DeletePolicy;

import java.util.List;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.NamePattern;

/**
 * @author gorbunkov
 */
@NamePattern("%s|name")
@Table(name = "BPM_PROC_DEFINITION")
@Entity(name = "bpm$ProcDefinition")
public class ProcDefinition extends StandardEntity {

    private static final long serialVersionUID = 8978443737036741675L;

    @Column(name = "NAME")
    protected String name;

    @Column(name = "ACT_ID")
    protected String actId;

    @Column(name = "ACT_KEY", unique = true)
    protected String actKey;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "procDefinition")
    @OrderBy("order")
    protected List<ProcRole> procRoles;

    @Column(name = "ACTIVE")
    protected Boolean active;

    @Column(name = "ACT_VERSION")
    protected Integer actVersion;

    @Column(name = "ACT_DEPLOYMENT_ID")
    protected String actDeploymentId;

    public void setActDeploymentId(String actDeploymentId) {
        this.actDeploymentId = actDeploymentId;
    }

    public String getActDeploymentId() {
        return actDeploymentId;
    }


    public void setActVersion(Integer actVersion) {
        this.actVersion = actVersion;
    }

    public Integer getActVersion() {
        return actVersion;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setProcRoles(List<ProcRole> procRoles) {
        this.procRoles = procRoles;
    }

    public List<ProcRole> getProcRoles() {
        return procRoles;
    }

    public void setActKey(String actKey) {
        this.actKey = actKey;
    }

    public String getActKey() {
        return actKey;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setActId(String actId) {
        this.actId = actId;
    }

    public String getActId() {
        return actId;
    }
}