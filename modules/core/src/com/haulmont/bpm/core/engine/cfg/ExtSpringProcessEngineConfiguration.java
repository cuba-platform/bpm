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

package com.haulmont.bpm.core.engine.cfg;

import com.haulmont.bpm.core.engine.impl.db.ExtDbSqlSessionFactory;
import com.haulmont.bpm.core.engine.spring.ExtSpringTransactionInterceptor;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

public class ExtSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ExtSpringProcessEngineConfiguration.class);

    protected static final Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();

    @Override
    protected CommandInterceptor createTransactionInterceptor() {
        if (transactionManager == null) {
            throw new ActivitiException(String.format(
                    "transactionManager is required property for SpringProcessEngineConfiguration, use %s otherwise",
                    StandaloneProcessEngineConfiguration.class.getName()));
        }

        return new ExtSpringTransactionInterceptor(transactionManager);
    }

    /**
     * HSQL database is not supported by Activiti out of the box. So we use H2 database syntax for HSQL as well.
     */
    protected static Properties getDefaultDatabaseTypeMappings() {
        Properties databaseTypeMappings = new Properties();
        databaseTypeMappings.putAll(ProcessEngineConfigurationImpl.databaseTypeMappings);
        databaseTypeMappings.setProperty("HSQL Database Engine", DATABASE_TYPE_H2);
        return databaseTypeMappings;
    }

    /**
     * The method is just a copy of the method from the parent class. It was necessary because we
     * use new {@code databaseTypeMappings} properties.
     */
    @Override
    public void initDatabaseType() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String databaseProductName = databaseMetaData.getDatabaseProductName();
            log.debug("database product name: '{}'", databaseProductName);
            databaseType = databaseTypeMappings.getProperty(databaseProductName);
            if (databaseType==null) {
                throw new ActivitiException("couldn't deduct database type from database product name '"+databaseProductName+"'");
            }
            log.debug("using database type: {}", databaseType);

        } catch (SQLException e) {
            log.error("Exception while initializing Database connection", e);
        } finally {
            try {
                if (connection!=null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Exception while closing the Database connection", e);
            }
        }
    }

    /**
     * Replaces DbSqlSessionFactory with our own implementation
     */
    @Override
    protected void initSessionFactories() {
        super.initSessionFactories();

        ExtDbSqlSessionFactory sessionFactory = new ExtDbSqlSessionFactory();
        sessionFactory.setDatabaseType(databaseType);
        sessionFactory.setIdGenerator(idGenerator);
        sessionFactory.setSqlSessionFactory(sqlSessionFactory);
        sessionFactory.setDbIdentityUsed(isDbIdentityUsed);
        sessionFactory.setDbHistoryUsed(isDbHistoryUsed);
        sessionFactory.setDatabaseTablePrefix(databaseTablePrefix);
        sessionFactory.setTablePrefixIsSchema(tablePrefixIsSchema);
        sessionFactory.setDatabaseCatalog(databaseCatalog);
        sessionFactory.setDatabaseSchema(databaseSchema);
        addSessionFactory(sessionFactory);
    }
}