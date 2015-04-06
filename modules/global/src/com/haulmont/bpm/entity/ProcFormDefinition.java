/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ProcFormDefinition implements Serializable {

    protected String name;

    protected Map<String, String> params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
