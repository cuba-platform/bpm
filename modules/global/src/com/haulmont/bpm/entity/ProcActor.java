/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.security.entity.User;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;
import javax.persistence.Column;

@NamePattern("%s (%s)|user,procRole")
@Table(name = "BPM_PROC_ACTOR")
@Entity(name = "bpm$ProcActor")
public class ProcActor extends StandardEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    protected User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROC_INSTANCE_ID")
    protected ProcInstance procInstance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROC_ROLE_ID")
    protected ProcRole procRole;

    private static final long serialVersionUID = -3771748975924652289L;

    @Column(name = "ORDER_")
    protected Integer order = 0;

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getOrder() {
        return order;
    }

    public void setProcInstance(ProcInstance procInstance) {
        this.procInstance = procInstance;
    }

    public ProcInstance getProcInstance() {
        return procInstance;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setProcRole(ProcRole procRole) {
        this.procRole = procRole;
    }

    public ProcRole getProcRole() {
        return procRole;
    }
}