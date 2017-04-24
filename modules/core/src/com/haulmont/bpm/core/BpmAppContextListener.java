/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.haulmont.cuba.core.sys.AppContext;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Finds all serviceTask custom stencils in the stencilset JSON and registers them at the CubaBpmnJsonConverter
 */
@Component("bpm_BpmAppContextListener")
public class BpmAppContextListener implements AppContext.Listener, Ordered {

    private final Logger log = LoggerFactory.getLogger(BpmAppContextListener.class);

    @Inject
    protected StencilSetManager stencilSetManager;

    @Inject
    protected ProcessEngineConfiguration processEngineConfiguration;

    public BpmAppContextListener() {
        AppContext.addListener(this);
    }

    @Override
    public void applicationStarted() {
        try {
            registerCustomStencils();
        } catch (Exception e) {
            log.error("Exception on registering custom stencils", e);
        }

        AsyncExecutor asyncExecutor = processEngineConfiguration.getAsyncExecutor();
        if (asyncExecutor != null) {
            asyncExecutor.start();
        }
    }

    @Override
    public void applicationStopped() {
    }

    protected void registerCustomStencils() {
        String stencilSet = stencilSetManager.getStencilSet();
        JsonParser jsonParser = new JsonParser();
        JsonObject rootJsonObject = jsonParser.parse(stencilSet).getAsJsonObject();
        JsonArray stencilsArray = rootJsonObject.getAsJsonArray("stencils");
        for (JsonElement stencilElement : stencilsArray) {
            JsonObject stencilJsonObject = stencilElement.getAsJsonObject();
            JsonObject customObject = stencilJsonObject.getAsJsonObject("custom");
            if (customObject != null) {
                String stencilId = stencilJsonObject.getAsJsonPrimitive("id").getAsString();
                stencilSetManager.registerServiceTaskStencilBpmnJsonConverter(stencilId);
            }
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PLATFORM_PRECEDENCE - 200;
    }
}
