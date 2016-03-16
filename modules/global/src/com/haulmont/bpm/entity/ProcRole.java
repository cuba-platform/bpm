/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.entity;

import com.google.common.base.Strings;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.PersistenceHelper;

import javax.persistence.*;

/**
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
        if (PersistenceHelper.isLoaded(this, "procDefinition") && procDefinition != null
                && PersistenceHelper.isLoaded(procDefinition, "actId") && !Strings.isNullOrEmpty(procDefinition.getActId())) {
            ProcessMessagesService processMessagesService = AppBeans.get(ProcessMessagesService.class);
            String locName = processMessagesService.findMessage(procDefinition.getActId(), code);
            if (locName != null) return locName;
        }
        return name;
    }



}