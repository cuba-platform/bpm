/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.core.jmx;

import com.haulmont.bpm.core.ProcessMessagesManager;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

/**
 * @author gorbunkov
 * @version $Id$
 */
@ManagedBean("bpm_MessagesCacheMBean")
public class MessagesCache implements MessagesCacheMBean {

    @Inject
    protected ProcessMessagesManager processMessagesManager;

    @Override
    public void clearCaches() {
        processMessagesManager.clearCaches();
    }
}
