/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import com.haulmont.cuba.core.entity.StandardEntity;
import javax.persistence.Lob;
import com.haulmont.chile.core.annotations.NamePattern;

@NamePattern("%s|name")
@Table(name = "BPM_PROC_MODEL")
@Entity(name = "bpm$ProcModel")
public class ProcModel extends StandardEntity {
    private static final long serialVersionUID = 4795058970950415977L;

    @Column(name = "NAME", nullable = false, unique = true)
    protected String name;

    @Column(name = "ACT_MODEL_ID")
    protected String actModelId;

    @Lob
    @Column(name = "DESCRIPTION")
    protected String description;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setActModelId(String actModelId) {
        this.actModelId = actModelId;
    }

    public String getActModelId() {
        return actModelId;
    }
}