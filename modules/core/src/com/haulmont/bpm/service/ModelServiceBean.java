/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.haulmont.bpm.exception.BpmException;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author gorbunkov
 * @version $Id$
 */
@Service(ModelService.NAME)
public class ModelServiceBean implements ModelService {

    @Inject
    protected RepositoryService repositoryService;

    protected static final Log log = LogFactory.getLog(ModelServiceBean.class);

    @Override
    public String getModelJson(String actModelId) {
        Model model = repositoryService.getModel(actModelId);
        if (model != null) {
            try {
                JSONObject resultJsonObject = new JSONObject();
                String modelJson = new String(repositoryService.getModelEditorSource(model.getId()), "utf-8");
                JSONObject modelJsonObject = new JSONObject(modelJson);
                resultJsonObject.put("modelId", model.getId());
                resultJsonObject.put("name", model.getName());
                resultJsonObject.put("model", modelJsonObject);
                return resultJsonObject.toString();
            } catch (Exception e) {
                log.error("Error creating model JSON", e);
                throw new BpmException("Error creating model JSON", e);
            }
        }
        return null;
    }

    @Override
    public void saveModel(String actModelId, String modelName, String modelDescription,
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

        try {
            repositoryService.saveModel(model);
            repositoryService.addModelEditorSource(model.getId(), editorNode.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new BpmException("Error creating new model", e);
        }

        return model.getId();
    }
}
