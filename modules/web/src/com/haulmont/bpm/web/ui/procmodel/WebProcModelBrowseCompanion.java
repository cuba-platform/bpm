/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.web.ui.procmodel;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.bpm.gui.procmodel.ProcModelBrowse;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.WebWindowManager;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.toolkit.ui.CubaButton;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.Extension;

public class WebProcModelBrowseCompanion implements ProcModelBrowse.Companion {

    @Override
    public void openModeler(String modelerUrl) {
        Messages messages = AppBeans.get(Messages.class);

        String notificationCaption = messages.getMessage(WebProcModelBrowseCompanion.class, "procModelerLink");

        String webAppUrl = ControllerUtils.getLocationWithoutParams();
        String url = webAppUrl + modelerUrl;
        String notificationBody = messages.getMessage(WebProcModelBrowseCompanion.class, "linkHtml");

        notificationBody = String.format(notificationBody, url);

        WebWindowManager windowManager = App.getInstance().getWindowManager();

        // try to open a window with modeler
        windowManager.showWebPage(url, ParamsMap.of("tryToOpenAsPopup", Boolean.TRUE));
        // show notification if user blocks popups
        windowManager.showNotification(notificationCaption, notificationBody, Frame.NotificationType.TRAY_HTML);
    }

    @Override
    public void setupModelerPopupOpener(Button button, String modelerUrl) {
        BrowserWindowOpener windowOpener = null;

        for (Extension extension : button.unwrap(CubaButton.class).getExtensions()) {
            if (extension instanceof BrowserWindowOpener) {
                ((BrowserWindowOpener) extension).setUrl(modelerUrl);
                windowOpener = ((BrowserWindowOpener) extension);
                break;
            }
        }

        if (windowOpener == null) {
            windowOpener = new BrowserWindowOpener(modelerUrl);
            windowOpener.extend(button.unwrap(CubaButton.class));
        }
    }
}