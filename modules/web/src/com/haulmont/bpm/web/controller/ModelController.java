/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.haulmont.bpm.entity.ProcModel;
import com.haulmont.bpm.rest.RestModel;
import com.haulmont.bpm.service.ModelService;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Controller is used by modeler for manipulating with models.
 *
 */
@Controller("bpm_ModelController")
@RequestMapping("/modeler/model")
public class ModelController {

    @Inject
    protected ModelService modelService;

    @Inject
    protected DataManager dataManager;

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

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public JsonNode getModels(HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        if (BpmControllerUtils.auth(request, response)) {
            LoadContext ctx = new LoadContext(ProcModel.class);
            ctx.setQueryString("select m from bpm$ProcModel m order by m.name");
            List<ProcModel> models = dataManager.loadList(ctx);
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (ProcModel model : models) {
                ObjectNode objectNode = objectMapper.createObjectNode();
                objectNode.put("actModelId", model.getActModelId());
                objectNode.put("name", model.getName());
                arrayNode.add(objectNode);
            }
            return arrayNode;
        }
        return null;
    }
}