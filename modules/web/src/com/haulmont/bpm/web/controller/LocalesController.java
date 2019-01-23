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
