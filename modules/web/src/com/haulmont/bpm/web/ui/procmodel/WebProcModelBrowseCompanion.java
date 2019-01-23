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

package com.haulmont.bpm.web.ui.procmodel;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.bpm.gui.procmodel.ProcModelBrowse;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.widgets.CubaButton;
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

        WindowManager windowManager = App.getInstance().getWindowManager();

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