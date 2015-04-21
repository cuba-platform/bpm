/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.core.engine.cfg;

import com.haulmont.bpm.core.engine.spring.ExtSpringTransactionInterceptor;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.spring.SpringProcessEngineConfiguration;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ExtSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

    @Override
    protected CommandInterceptor createTransactionInterceptor() {
        if (transactionManager == null) {
            throw new ActivitiException("transactionManager is required property for SpringProcessEngineConfiguration, use "
                    + StandaloneProcessEngineConfiguration.class.getName() + " otherwise");
        }

        return new ExtSpringTransactionInterceptor(transactionManager);
    }
}
