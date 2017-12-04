/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.web.controller;

import com.haulmont.cuba.core.global.GlobalConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller returns a list of available locales. It is used by the modeler localization page.
 */
@RestController("bpm_LocalesController")
@RequestMapping("/modeler/locales")
public class LocalesController {

    @Inject
    protected GlobalConfig globalConfig;

    @GetMapping
    public Set<String> getLocales(HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        if (BpmControllerUtils.auth(request, response)) {
            return globalConfig.getAvailableLocales().values().stream().map(Locale::toString).collect(Collectors.toSet());
        }
        return null;

    }
}
