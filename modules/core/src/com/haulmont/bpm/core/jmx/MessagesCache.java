/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
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
