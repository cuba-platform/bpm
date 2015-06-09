/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.web.controller;

import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author gorbunkov
 * @version $Id$
 */
@Controller
@RequestMapping("/modeler/form")
public class FormController {

    @Inject
    protected ProcessFormService processFormService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<ProcFormDefinition> getAllForms(HttpServletRequest request,
                                                HttpServletResponse response) throws IOException {
        if (BpmControllerUtils.auth(request, response)) {
            return processFormService.getAllForms();
        }
        return null;
    }
}
