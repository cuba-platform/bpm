/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core.jmx;

import com.haulmont.bpm.core.ProcessMessagesManager;

import org.springframework.stereotype.Component;
import javax.inject.Inject;

/**
 */
@Component("bpm_MessagesCacheMBean")
public class MessagesCache implements MessagesCacheMBean {

    @Inject
    protected ProcessMessagesManager processMessagesManager;

    @Override
    public void clearCaches() {
        processMessagesManager.clearCaches();
    }
}
