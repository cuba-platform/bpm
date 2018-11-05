/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

@Source(type = SourceType.APP)
public interface BpmConfig extends Config {
    @Property("bpm.modeler.url")
    @Default("/modeler/modeler.html")
    String getModelerUrl();

    @Property("bpm.formsConfig")
    @Default("com/haulmont/bpm/forms.xml")
    String getFormsConfig();
}