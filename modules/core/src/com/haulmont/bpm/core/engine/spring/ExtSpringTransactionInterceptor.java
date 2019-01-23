/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
