/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.core.engine.cfg;

import com.haulmont.bpm.core.engine.impl.db.ExtDbSqlSessionFactory;
import com.haulmont.bpm.core.engine.spring.ExtSpringTransactionInterceptor;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ExtSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

    private static Logger log = LoggerFactory.getLogger(ExtSpringProcessEngineConfiguration.class);

    protected static Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();

    @Override
    protected CommandInterceptor createTransactionInterceptor() {
        if (transactionManager == null) {
            throw new ActivitiException("transactionManager is required property for SpringProcessEngineConfiguration, use "
                    + StandaloneProcessEngineConfiguration.class.getName() + " otherwise");
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
        sessionFactory.setOptimizeDeleteOperationsEnabled(isOptimizeDeleteOperationsEnabled);
        addSessionFactory(sessionFactory);
    }
}
