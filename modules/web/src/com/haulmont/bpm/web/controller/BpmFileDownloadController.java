/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.web.controller;

import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.controllers.FileDownloadController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller("bpm_BpmFileDownloadController")
public class BpmFileDownloadController extends FileDownloadController {

    /**
     * Method is used by the BPMN modeler for extracting custom stencil icon
     */
    @Override
    @RequestMapping(value = "/modeler/icon", method = RequestMethod.GET)
    public ModelAndView download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return super.download(request, response);
    }

    @Override
    protected UserSession getSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ControllerUtils.getUserSession(request);
    }
}
