/*
 * Copyright (c) 2015 com.haulmont.bpm.entity
 */
package com.haulmont.bpm.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;

/**
 * @author gorbunkov
 */
@NamePattern("%s|name")
@Table(name = "BPM_PROC_ATTACHMENT_TYPE")
@Entity(name = "bpm$ProcAttachmentType")
public class ProcAttachmentType extends StandardEntity {
    @Column(name = "NAME", nullable = false)
    protected String name;

    @Column(name = "CODE")
    protected String code;

    private static final long serialVersionUID = 5599792799275318734L;

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


}