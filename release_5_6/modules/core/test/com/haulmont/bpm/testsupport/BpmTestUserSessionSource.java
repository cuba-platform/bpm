/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.testsupport;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.sys.AbstractUserSessionSource;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class BpmTestUserSessionSource extends AbstractUserSessionSource {

    public static final String USER_ID = "60885987-1b61-4247-94c7-dff348347f93";

    private UserSession session;

    @Override
    public boolean checkCurrentUserSession() {
        return true;
    }

    @Override
    public synchronized UserSession getUserSession() {
        if (session == null) {
            User user = getUser();
            session = new UserSession(UUID.randomUUID(), user, Collections.<Role>emptyList(), Locale.forLanguageTag("en"), false);
        }
        return session;
    }

    public void setUserSession(UserSession session) {
        this.session = session;
    }

    protected User getUser() {
        EntityManagerFactory jpaEmf = AppBeans.get("entityManagerFactory");
        OpenJPAEntityManager jpaEm = (OpenJPAEntityManager)
                EntityManagerFactoryUtils.doGetTransactionalEntityManager(jpaEmf, null);
        return jpaEm.find(User.class, UUID.fromString(USER_ID));
    }
}
