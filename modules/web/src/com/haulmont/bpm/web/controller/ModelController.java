/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.haulmont.bpm.rest.RestModel;
import com.haulmont.bpm.service.ModelService;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller is used by modeler for manipulating with models
 * @author gorbunkov
 * @version $Id$
 */

@Controller
@RequestMapping("/modeler/model")
public class ModelController {

    @Inject
    protected ModelService modelService;

    @RequestMapping(value = "/{actModelId}", method = RequestMethod.GET)
    @ResponseBody
    public JsonNode getModel(@PathVariable String actModelId,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        if (BpmControllerUtils.auth(request, response)) {
            RestModel restModel = modelService.getModelJson(actModelId);
            if (restModel == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("modelId", restModel.getModelId());
            objectNode.put("name", restModel.getName());
            JsonNode modelNode = objectMapper.readTree(restModel.getModelJson());
            objectNode.set("model", modelNode);
            return objectNode;
        }
        return null;
    }

    @RequestMapping(value = "/{actModelId}", method = RequestMethod.PUT)
    public void updateModel(@PathVariable String actModelId,
                            @RequestBody MultiValueMap<String, String> values,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        if (BpmControllerUtils.auth(request, response)) {
            String modelName = values.getFirst("name");
            String modelDescription = values.getFirst("description");
            String modelJsonStr = values.getFirst("json_xml");
            String modelSvgStr = values.getFirst("svg_xml");
            modelService.updateModel(actModelId, modelName, modelDescription, modelJsonStr, modelSvgStr);
        }
    }
}
