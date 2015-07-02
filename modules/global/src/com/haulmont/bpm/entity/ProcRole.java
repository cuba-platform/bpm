/*
 * Copyright (c) 2015 com.haulmont.bpm.entity
 */
package com.haulmont.bpm.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.google.common.base.Strings;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.global.AppBeans;

/**
 * @author gorbunkov
 */
@NamePattern("#getLocName|procDefinition,code,name")
@Table(name = "BPM_PROC_ROLE")
@Entity(name = "bpm$ProcRole")
public class ProcRole extends StandardEntity {
    @Column(name = "NAME", nullable = false)
    protected String name;

    @Column(name = "CODE", nullable = false)
    protected String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROC_DEFINITION_ID")
    protected ProcDefinition procDefinition;

    private static final long serialVersionUID = 1729895116848728087L;

    @Column(name = "ORDER_")
    protected Integer order;

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getOrder() {
        return order;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setProcDefinition(ProcDefinition procDefinition) {
        this.procDefinition = procDefinition;
    }

    public ProcDefinition getProcDefinition() {
        return procDefinition;
    }

    @MetaProperty
    public String getLocName() {
        if (procDefinition != null && !Strings.isNullOrEmpty(procDefinition.getActId())) {
            ProcessMessagesService processMessagesService = AppBeans.get(ProcessMessagesService.class);
            String locName = processMessagesService.findMessage(procDefinition.getActId(), code);
            if (locName != null) return locName;
        }
        return name;
    }



}