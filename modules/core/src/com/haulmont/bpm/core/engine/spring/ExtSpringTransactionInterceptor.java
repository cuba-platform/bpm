/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.core.engine.spring;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.security.app.Authentication;
import org.activiti.engine.impl.cmd.ExecuteAsyncJobCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.spring.SpringTransactionInterceptor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * SpringTransactionInterceptor that sets securityContext for asynchronous jobs.
 * It is necessary for timer boundary events that modify entities.
 * @author gorbunkov
 * @version $Id$
 */
public class ExtSpringTransactionInterceptor extends SpringTransactionInterceptor {

    protected Authentication authentication;

    public ExtSpringTransactionInterceptor(PlatformTransactionManager transactionManager) {
        super(transactionManager);
    }

    @Override
    public <T> T execute(CommandConfig config, Command<T> command) {
        boolean instanceOfExecuteAsyncJobCmd = command instanceof ExecuteAsyncJobCmd;

        if (instanceOfExecuteAsyncJobCmd) {
            getAuthentication().begin();
        }

        T result = super.execute(config, command);

        if (instanceOfExecuteAsyncJobCmd) {
            getAuthentication().end();
        }
        return result;
    }

    protected Authentication getAuthentication() {
        if (authentication == null)
            authentication = AppBeans.get(Authentication.class);
        return authentication;
    }
}
