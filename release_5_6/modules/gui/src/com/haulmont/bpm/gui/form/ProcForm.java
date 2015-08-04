/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.form;

import java.util.Map;

/**
 * Interface that should be implemented by process forms.
 * @author gorbunkov
 * @version $Id$
 */
public interface ProcForm {

    /**
     * @return comment to be set to {@link com.haulmont.bpm.entity.ProcTask} instance
     */
    String getComment();

    /**
     * @return a map that will be added to activiti process variables
     */
    Map<String, Object> getFormResult();
}
