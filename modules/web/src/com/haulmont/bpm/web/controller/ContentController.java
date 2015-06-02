/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.web.controller;

import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.controllers.StaticContentController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller returns static content necessary for activiti modeler
 * @author gorbunkov
 */
@Controller
@RequestMapping(value = "/modeler/**")
public class ContentController extends StaticContentController {

    @Inject
    protected UserSessionSource userSessionSource;

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