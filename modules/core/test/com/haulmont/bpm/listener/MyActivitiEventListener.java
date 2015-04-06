/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.haulmont.bpm.listener;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class MyActivitiEventListener implements ActivitiEventListener {

    @Override
    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
            case TASK_CREATED:
                System.out.println("task created");
                throw new RuntimeException("Task aborted");
        }
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }
}
