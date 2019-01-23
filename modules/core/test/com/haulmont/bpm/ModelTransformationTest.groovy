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

package com.haulmont.bpm

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.haulmont.bpm.core.ModelTransformer
import com.haulmont.bpm.service.ModelService
import com.haulmont.bpm.testsupport.BpmTestContainer
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.Resources
import org.junit.After
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 *
 */
class ModelTransformationTest {

    ModelService modelService

    @ClassRule
    public static BpmTestContainer cont = new BpmTestContainer();

    @After
    public void after() {
        cont.cleanUpDatabase();
    }

    @Before
    void setUp() {
        modelService = AppBeans.get(ModelService.class)
    }

    @Test
    void testSubModelExpanding() {
        def subModelActId = uploadModel("subModel", [:])
        def mainModelActId = uploadModel("mainModel", ["{SUB_MODEL_ACT_ID}": subModelActId])
        def modelTransformer = AppBeans.get(ModelTransformer.class)

        def bytes = cont.repositoryService.getModelEditorSource(mainModelActId)
        def transformedJson = modelTransformer.transformModel(bytes)
        def objectMapper = new ObjectMapper()
        def objectNode = objectMapper.readTree(transformedJson)
        ArrayNode shapes = objectNode.get("childShapes") as ArrayNode
        def mainScript1 = findById(shapes, "mainScript1")
        def mainScript2 = findById(shapes, "mainScript2")
        def subScript1 = findById(shapes, "subScript1")
        def subScript2 = findById(shapes, "subScript2")

        def flow1 = findOutgoing(shapes, mainScript1)
        def afterMainScript1 = findOutgoing(shapes, flow1)
        assertEquals(afterMainScript1.get("resourceId").asText(), subScript1.get("resourceId").asText())

        def flow2 = findOutgoing(shapes, subScript2)
        def afterSubScript2 = findOutgoing(shapes, flow2)
        assertEquals(afterSubScript2.get("resourceId").asText(), mainScript2.get("resourceId").asText())
    }

    protected String uploadModel(String modelName, Map replacements) {
        String modelActId = modelService.createModel(modelName)
        def resources = AppBeans.get(Resources.class)
        def modelJson = resources.getResourceAsString("com/haulmont/bpm/model/${modelName}.json")
        replacements.each {k, v ->
            modelJson = modelJson.replace(k, v)
        }
        modelService.updateModel(modelActId, modelName, "", modelJson, "")
        return modelActId
    }

    protected JsonNode findById(ArrayNode shapes, String id) {
        shapes.find {it.get("properties").get("overrideid").asText() == id};
    }

    protected JsonNode findByResourceId(ArrayNode shapes, String id) {
        shapes.find {it.get("resourceId").asText() == id};
    }

    protected JsonNode findOutgoing(ArrayNode shapes, JsonNode node) {
        def outgoingResourceId = node.get("outgoing")[0].get("resourceId").asText()
        findByResourceId(shapes, outgoingResourceId)
    }
}
