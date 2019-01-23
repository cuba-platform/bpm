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

package com.haulmont.bpm.core;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.haulmont.bpm.exception.BpmException;
import org.activiti.engine.RepositoryService;

import org.springframework.stereotype.Component;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class is used for transforming a model JSON before creating a process XML from it
 */
@Component
public class ModelTransformer {

    @Inject
    protected RepositoryService repositoryService;

    /**
     * Finds all SubModel elements and replaces them with sub-model content.
     * SubModel elements are removed. Incoming flow nodes will point to the first
     * element of sub-model. Outgoing flow nodes will start from the last node of the sub-model.
     * @param model byte array with model JSON
     * @return modified model JSON
     */
    public String transformModel(byte[] model) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ObjectNode objectNode = (ObjectNode) objectMapper.readTree(model);
            ArrayNode srcChildShapes = (ArrayNode) objectNode.get("childShapes");
            ArrayNode resultChildShapes = objectMapper.createArrayNode();
            Map<JsonNode, ExpandedModelInfo> subModulesMap = new HashMap<>();
            if (srcChildShapes != null) {
                for (JsonNode childShape : srcChildShapes) {
                    if ("SubModel".equals(getStencilId(childShape))) {
                        String subModelName = childShape.get("properties").get("submodel").get("actModelId").asText();
                        ExpandedModelInfo modelInfo = getExpandedSubModelInfo(subModelName);
                        resultChildShapes.addAll(modelInfo.getShapes());
                        subModulesMap.put(childShape, modelInfo);
                    } else {
                        resultChildShapes.add(childShape);
                    }
                }
            }

            for (Map.Entry<JsonNode, ExpandedModelInfo> entry : subModulesMap.entrySet()) {
                JsonNode subModelNode = entry.getKey();
                ExpandedModelInfo subModelInfo = entry.getValue();
                JsonNode incomeFlow = findByOutgoingResourceId(resultChildShapes, getResourceId(subModelNode));
                JsonNode firstNode = subModelInfo.getFirstNode();
                ((ObjectNode)incomeFlow.get("outgoing").get(0)).replace("resourceId", firstNode.get("resourceId"));
                ((ObjectNode)incomeFlow.get("target")).replace("resourceId", firstNode.get("resourceId"));

                JsonNode outcomeFlow = findByResourceId(resultChildShapes, getOutgoingResourceId(subModelNode));
                JsonNode lastNode = subModelInfo.getLastNode();
                ((ObjectNode) lastNode.get("outgoing").get(0)).replace("resourceId", outcomeFlow.get("resourceId"));
            }

            objectNode.replace("childShapes", resultChildShapes);
            return objectMapper.writeValueAsString(objectNode);
        } catch (IOException e) {
            throw new BpmException("Error when transforming json model", e);
        }
    }

    protected ExpandedModelInfo getExpandedSubModelInfo(String actSubModelId) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode objectNode = objectMapper.readTree(repositoryService.getModelEditorSource(actSubModelId));
        ArrayNode childShapes = (ArrayNode) objectNode.get("childShapes");

        JsonNode startNode = findByStencilId(childShapes, "StartNoneEvent");
        JsonNode endNode = findByStencilId(childShapes, "EndNoneEvent");

        String firstFlowNodeResourceId = getOutgoingResourceId(startNode);
        String endResourceId = getResourceId(endNode);

        JsonNode firstFlowNode = findByResourceId(childShapes, firstFlowNodeResourceId);
        JsonNode lastFlowNode = findByOutgoingResourceId(childShapes, endResourceId);

        List<String> resourceIdsToExclude = new ArrayList<>();
        resourceIdsToExclude.add(getResourceId(startNode));
        resourceIdsToExclude.add(getResourceId(endNode));
        resourceIdsToExclude.add(getResourceId(firstFlowNode));
        resourceIdsToExclude.add(getResourceId(lastFlowNode));

        ArrayNode subModelArrayNode = objectMapper.createArrayNode();
        for (JsonNode shapeNode : childShapes) {
            if (!resourceIdsToExclude.contains(shapeNode.get("resourceId").asText())) {
                subModelArrayNode.add(shapeNode);
            }
        }

        JsonNode firstNode = findByResourceId(childShapes, getOutgoingResourceId(firstFlowNode));
        JsonNode lastNode = findByOutgoingResourceId(childShapes, getResourceId(lastFlowNode));

        return new ExpandedModelInfo(subModelArrayNode, firstNode, lastNode);
    }

    @Nullable
    protected String getStencilId(JsonNode shapeNode) {
        JsonNode stencilNode = shapeNode.get("stencil");
        if (stencilNode != null) {
            JsonNode idNode = stencilNode.get("id");
            if (idNode != null) return idNode.asText();
        }
        return null;
    }

    @Nullable
    protected JsonNode findByStencilId(JsonNode childShapesNode, String stencilId) {
        for (JsonNode childShape : childShapesNode) {
            String currentStencilId = getStencilId(childShape);
            if (stencilId.equals(currentStencilId)) {
                return childShape;
            }
        }
        return null;
    }

    @Nullable
    protected JsonNode findByOutgoingResourceId(ArrayNode arrayNode, String targetResourceId) {
        for (JsonNode childShape : arrayNode) {
            ArrayNode outgoing = (ArrayNode) childShape.get("outgoing");
            if (outgoing.size() == 0) continue;
            String currentTarget = outgoing.get(0).get("resourceId").asText();
            if (targetResourceId.equals(currentTarget)) return childShape;
        }
        return null;
    }

    @Nullable
    protected JsonNode findByResourceId(ArrayNode arrayNode, String resourceId) {
        for (JsonNode childShape : arrayNode) {
            if (resourceId.equals(childShape.get("resourceId").asText())) {
                return childShape;
            }
        }
        return null;
    }

    protected String getResourceId(JsonNode jsonNode) {
        return jsonNode.get("resourceId").asText();
    }

    protected String getOutgoingResourceId(JsonNode jsonNode) {
        return jsonNode.get("outgoing").get(0).get("resourceId").asText();
    }

    /**
     * Data structure that stores a list of sub-model shapes that will be added to the
     * main model, first sub-model node and the last sub-model node (required for modifying flow
     * nodes in the main model)
     */
    protected class ExpandedModelInfo {
        protected ArrayNode shapes;
        protected JsonNode firstNode;
        protected JsonNode lastNode;

        public ExpandedModelInfo(ArrayNode shapes, JsonNode firstNode, JsonNode lastNode) {
            this.shapes = shapes;
            this.firstNode = firstNode;
            this.lastNode = lastNode;
        }

        public ArrayNode getShapes() {
            return shapes;
        }

        public JsonNode getFirstNode() {
            return firstNode;
        }

        public JsonNode getLastNode() {
            return lastNode;
        }
    }
}
