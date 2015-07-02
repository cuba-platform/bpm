/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.haulmont.bpm.exception.BpmException;
import com.haulmont.bpm.rest.RestModel;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * @author gorbunkov
 * @version $Id$
 */
@Service(ModelService.NAME)
public class ModelServiceBean implements ModelService {

    @Inject
    protected RepositoryService repositoryService;

    @Inject
    protected Persistence persistence;

    protected static final Log log = LogFactory.getLog(ModelServiceBean.class);

    @Override
    public RestModel getModelJson(String actModelId) {
        Model model = repositoryService.getModel(actModelId);
        if (model != null) {
            try {
                String modelJson = new String(repositoryService.getModelEditorSource(model.getId()), "utf-8");
                return new RestModel(model.getId(), model.getName(), modelJson);
            } catch (Exception e) {
                log.error("Error creating model JSON", e);
                throw new BpmException("Error creating model JSON", e);
            }
        }
        return null;
    }

    @Override
    public void updateModel(String actModelId, String modelName, String modelDescription,
                          String modelJsonStr, String modelSvgStr) {
        Model model = repositoryService.getModel(actModelId);

        JSONObject modelJsonObject = new JSONObject(model.getMetaInfo());

        modelJsonObject.put("name", modelName);
        modelJsonObject.put("description", modelDescription);
        model.setMetaInfo(modelJsonObject.toString());
        model.setName(modelName);

        repositoryService.saveModel(model);
        try {
            repositoryService.addModelEditorSource(model.getId(), modelJsonStr.getBytes("utf-8"));
            //todo gorbunkov store svg
//            InputStream svgStream = new ByteArrayInputStream(modelSvgStr.getBytes("utf-8"));
//            TranscoderInput input = new TranscoderInput(svgStream);
//
//            PNGTranscoder transcoder = new PNGTranscoder();
//            // Setup output
//            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            TranscoderOutput output = new TranscoderOutput(outStream);
//
//            // Do the transformation
//            transcoder.transcode(input, output);
//            final byte[] result = outStream.toByteArray();
//            repositoryService.addModelEditorSourceExtra(model.getId(), result);
//            outStream.close();
        } catch (UnsupportedEncodingException e) {
            throw new BpmException("Error when saving model", e);
        }
    }

    @Override
    public void updateModel(String actModelId, String modelName, String modelDescription) {
        Model model = repositoryService.getModel(actModelId);
        if (model == null) return;
        JSONObject modelJsonObject = new JSONObject(model.getMetaInfo());
        modelJsonObject.put("name", modelName);
        modelJsonObject.put("description", modelDescription);
        model.setMetaInfo(modelJsonObject.toString());
        model.setName(modelName);
        repositoryService.saveModel(model);
    }

    @Override
    @Transactional
    public String createModel(String name) {
        Model model = repositoryService.newModel();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put("name", name);
        modelObjectNode.put("revision", 1);
        modelObjectNode.put("description", "");
        model.setMetaInfo(modelObjectNode.toString());
        model.setName(name);

        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);

        ObjectNode propertiesNode = objectMapper.createObjectNode();
        propertiesNode.put("process_id", toCamelCase(name));
        propertiesNode.put("name", name);

        fillEventListeners(objectMapper, propertiesNode);

        editorNode.put("properties", propertiesNode);

        try {
            repositoryService.saveModel(model);
            repositoryService.addModelEditorSource(model.getId(), editorNode.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new BpmException("Error creating new model", e);
        }

        return model.getId();
    }

    protected void fillEventListeners(ObjectMapper objectMapper, ObjectNode propertiesNode) {
        ObjectNode eventListenerNode = objectMapper.createObjectNode();
        eventListenerNode.put("className", "com.haulmont.bpm.core.engine.listener.BpmActivitiListener");
        eventListenerNode.put("implementation", "com.haulmont.bpm.core.engine.listener.BpmActivitiListener");
        eventListenerNode.put("event", getListenerEventTypesString());
        ArrayNode eventListenersArray = objectMapper.createArrayNode();
        eventListenersArray.add(eventListenerNode);
        ObjectNode eventListenersNode = objectMapper.createObjectNode();
        eventListenersNode.put("eventListeners", eventListenersArray);
        propertiesNode.put("eventlisteners", eventListenersNode);

        ArrayNode eventsArrayNode = objectMapper.createArrayNode();
        for (ActivitiEventType eventType : getActivitiEventTypes()) {
            ObjectNode eventNode = objectMapper.createObjectNode();
            eventNode.put("event", eventType.toString());
            eventsArrayNode.add(eventNode);
        }
        eventListenerNode.put("events", eventsArrayNode);
    }

    protected String toCamelCase(String sentence) {
        StringBuilder sb = new StringBuilder();
        String[] words = sentence.split("\\s+");
        sb.append(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            sb.append(StringUtils.capitalize(words[i].toLowerCase()));
        }
        return sb.toString();
    }

    protected String getListenerEventTypesString() {
        List<ActivitiEventType> activitiEventTypes = getActivitiEventTypes();

        return Joiner.on(", ").join(activitiEventTypes);
    }

    protected List<ActivitiEventType> getActivitiEventTypes() {
        return Arrays.asList(
                ActivitiEventType.TASK_CREATED,
                ActivitiEventType.TASK_ASSIGNED,
                ActivitiEventType.PROCESS_COMPLETED,
                ActivitiEventType.TIMER_FIRED,
                ActivitiEventType.ACTIVITY_CANCELLED);
    }

    @Override
    public String updateModelNameInJson(String json, String modelName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode objectNode = objectMapper.readTree(json);
            ObjectNode properties = (ObjectNode) objectNode.get("properties");
            properties.put("name", modelName);
            properties.put("process_id", toCamelCase(modelName));
            return objectNode.toString();
        } catch (IOException e) {
            throw new BpmException("Error when update model name in JSON", e);
        }
    }
}
