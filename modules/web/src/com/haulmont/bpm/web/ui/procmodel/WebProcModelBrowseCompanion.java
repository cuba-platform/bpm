/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.web.ui.procmodel;

import com.haulmont.bpm.gui.procmodel.ProcModelBrowse;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.controllers.ControllerUtils;

import java.util.Collections;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class WebProcModelBrowseCompanion implements ProcModelBrowse.Companion {

    @Override
    public void openModeler(String modelerUrl) {
        String webAppUrl = ControllerUtils.getLocationWithoutParams();
        String url = webAppUrl + modelerUrl;
        App.getInstance().getWindowManager().showWebPage(url, Collections.<String, Object>singletonMap("tryToOpenAsPopup", Boolean.TRUE));
    }
}
