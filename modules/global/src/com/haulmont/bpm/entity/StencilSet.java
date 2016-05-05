/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity(name = "bpm$StencilSet")
@Table(name = "BPM_STENCIL_SET")
public class StencilSet extends StandardEntity {

    private static final long serialVersionUID = -3420632200646053615L;

    @Column(name = "NAME", nullable = false, unique = true)
    protected String name;

    @Lob
    @Column(name = "JSON_DATA", nullable = false)
    protected String jsonData;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
