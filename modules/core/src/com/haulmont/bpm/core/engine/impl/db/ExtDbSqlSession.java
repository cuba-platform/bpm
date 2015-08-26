/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.core.engine.impl.db;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;

import java.sql.Connection;

/**
 * Overriden {@code DbSqlSession} has an empty overriden {@code performSchemaOperationsProcessEngineBuild} method
 * because we don't want Activiti engine to manage its database schema.
 * <p>
 * If default DbSqlSession is used then an exception is thrown when we run an application server with just added BPM
 * module for the first time. {@code performSchemaOperationsProcessEngineBuild()} queries the database, but the DbUpdater
 * hasn't created activiti tables by that time.
 * </p>
 * @author gorbunkov
 * @version $Id$
 */
public class ExtDbSqlSession extends DbSqlSession {
    public ExtDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
        super(dbSqlSessionFactory);
    }

    public ExtDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, Connection connection, String catalog, String schema) {
        super(dbSqlSessionFactory, connection, catalog, schema);
    }

    @Override
    public void performSchemaOperationsProcessEngineBuild() {
    }
}
