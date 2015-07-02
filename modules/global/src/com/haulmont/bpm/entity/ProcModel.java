/*
 * Copyright (c) 115 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */
package com.haulmont.bpm.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import com.haulmont.cuba.core.entity.StandardEntity;
import javax.persistence.Lob;
import com.haulmont.chile.core.annotations.NamePattern;

/**
 * @author gorbunkov
 */
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