/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

/**
 * @author gorbunkov
 * @version $Id$
 */
@Source(type = SourceType.APP)
public interface BpmConfig extends Config {
    @Property("bpm.modeler.url")
    @Default("/modeler/modeler.html")
    String getModelerUrl();
}
