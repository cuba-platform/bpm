/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.web.controller;

import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.controllers.StaticContentController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller returns static content necessary for activiti modeler
 */
@Controller("bpm_ContentController")
@RequestMapping(value = "/modeler/**")
public class ContentController extends StaticContentController {

    @Override
    public String handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkUserSession(request, response)) {
            return super.handleGetRequest(request, response);
        }
        return null;
    }

    @Override
    public String handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkUserSession(request, response)) {
            return super.handlePostRequest(request, response);
        }
        return null;
    }

    @Override
    public String handleHeadRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkUserSession(request, response)) {
            return super.handleHeadRequest(request, response);
        }
        return null;
    }

    protected boolean checkUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserSession userSession = ControllerUtils.getUserSession(request);
        if (userSession != null) {
            return true;
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
    }
}