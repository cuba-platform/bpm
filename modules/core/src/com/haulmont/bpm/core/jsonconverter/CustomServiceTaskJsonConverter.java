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

package com.haulmont.bpm.core.jsonconverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.editor.language.json.converter.BpmnJsonConverterUtil;
import org.activiti.editor.language.json.converter.ServiceTaskJsonConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * <p>Class is used for converting a JSON representation of the custom service task to the
 * Activiti internal model class. That model class will be then used for generation of BPMN
 * process XML.</p>
 * <p>Class parses a 'custom' property of the JSON object and builds a middleware bean invocation expression.</p>
 */
public class CustomServiceTaskJsonConverter extends ServiceTaskJsonConverter {

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        ServiceTask task = new ServiceTask();

        String expression = createExpression(elementNode);
        task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
        task.setImplementation(expression);

        if (StringUtils.isNotEmpty(getPropertyValueAsString(PROPERTY_SERVICETASK_RESULT_VARIABLE, elementNode))) {
            task.setResultVariableName(getPropertyValueAsString(PROPERTY_SERVICETASK_RESULT_VARIABLE, elementNode));
        }

        return task;
    }

    /**
     * Custom service task element has a 'custom' property with the following structure:
     * <pre>
     * "custom" : {
     *     "beanName" : "app_MyBean",
     *     "methodName" : "someMethod"
     *     "methodArgs: : [
     *          {
     *              "propertyPackageName" : "mystencil-myproperty1package"
     *              "type" : "string"
     *          },
     *          {
     *              "propertyPackageName" : "mystencil-myproperty2package"
     *              "type" : "number"
     *          }
     * }
     * </pre>
     *
     * The method will return an expression like '${app_MyBean.someMethod('property1Value', property2Value)}
     * that can be used to invoke middleware bean method.
     *
     * @return bean method invocation expression
     */
    protected String createExpression(JsonNode elementNode) {
        JsonNode customNode = elementNode.get("custom");

        StringBuilder sb = new StringBuilder();
        String beanName = BpmnJsonConverterUtil.getValueAsString("beanName", customNode);
        String methodName = BpmnJsonConverterUtil.getValueAsString("methodName", customNode);
        JsonNode methodArgs = customNode.get("methodArgs");

        sb.append("${").append(beanName)
                .append(".")
                .append(methodName)
                .append("(");

        Iterator<JsonNode> iterator = methodArgs.iterator();
        List<String> paramValues = new ArrayList<>();
        while (iterator.hasNext()) {
            JsonNode paramNode = iterator.next();
            String propertyPackageName = BpmnJsonConverterUtil.getValueAsString("propertyPackageName", paramNode);
            String type = BpmnJsonConverterUtil.getValueAsString("type", paramNode);
            String propertyPackageId = propertyPackageName.substring(0, propertyPackageName.lastIndexOf("package"));
            String paramValue = BpmnJsonConverterUtil.getPropertyValueAsString(propertyPackageId, elementNode);
            if ("java.lang.String".equals(type))
                paramValue = "'" + paramValue + "'";
            paramValues.add(paramValue);
        }

        sb.append(Joiner.on(",").join(paramValues))
                .append(")").append("}");
        return sb.toString();
    }
}
