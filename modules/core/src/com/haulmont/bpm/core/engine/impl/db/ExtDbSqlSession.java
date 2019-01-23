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
