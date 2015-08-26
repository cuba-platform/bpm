/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.core.engine.impl.db;

import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.interceptor.Session;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ExtDbSqlSessionFactory extends DbSqlSessionFactory {
    @Override
    public Session openSession() {
        return new ExtDbSqlSession(this);
    }
}
