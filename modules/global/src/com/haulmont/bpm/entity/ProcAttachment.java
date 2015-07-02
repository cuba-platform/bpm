/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import com.haulmont.cuba.security.entity.User;

/**
 * @author gorbunkov
 */
@Table(name = "BPM_PROC_ATTACHMENT")
@Entity(name = "bpm$ProcAttachment")
public class ProcAttachment extends StandardEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FILE_ID")
    protected FileDescriptor file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_ID")
    protected ProcAttachmentType type;

    @Lob
    @Column(name = "COMMENT_")
    protected String comment;

    private static final long serialVersionUID = -3032930359790118895L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_INSTANCE_ID")
    protected ProcInstance procInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_TASK_ID")
    protected ProcTask procTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHOR_ID")
    protected User author;

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getAuthor() {
        return author;
    }


    public void setProcTask(ProcTask procTask) {
        this.procTask = procTask;
    }

    public ProcTask getProcTask() {
        return procTask;
    }


    public void setProcInstance(ProcInstance procInstance) {
        this.procInstance = procInstance;
    }

    public ProcInstance getProcInstance() {
        return procInstance;
    }


    public void setFile(FileDescriptor file) {
        this.file = file;
    }

    public FileDescriptor getFile() {
        return file;
    }

    public void setType(ProcAttachmentType type) {
        this.type = type;
    }

    public ProcAttachmentType getType() {
        return type;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }


}