/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.web.controller;

import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.gui.app.ProcessFormRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/modeler/form")
public class FormController {

    @Inject
    protected ProcessFormRepository processFormRepository;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<ProcFormDefinition> getAllForms(HttpServletRequest request,
                                                HttpServletResponse response) throws IOException {
        if (BpmControllerUtils.auth(request, response)) {
            return processFormRepository.getForms();
        }
        return null;
    }
}