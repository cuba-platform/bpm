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
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("bpm_BpmAppContextListener")
public class BpmAppContextListener implements AppContext.Listener {

    @Inject
    protected StencilSetManager stencilSetManager;

    public BpmAppContextListener() {
        AppContext.addListener(this);
    }

    @Override
    public void applicationStarted() {
        registerCustomStencils();
    }

    @Override
    public void applicationStopped() {
    }

    /**
     * Finds all serviceTask custom stencils in the stencilset JSON and registers them at the CubaBpmnJsonConverter
     */
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
}
