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

package com.haulmont.bpm.core.engine.parser;

import com.google.common.base.Strings;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.task.TaskDefinition;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The handler automatically sets some UserTask expressions:
 * <ul>
 *     <li>assignee -  for single user task based on procRole field</li>
 *     <li>candidateUsers -  for single user task based on procRole field if claimAllowed set to "true" for the task</li>
 *     <li>full bunch of loopCharacteristics - for multiInstance task  based on procRole field</li>
 * </ul>
 */
public class BpmUserTaskParseHandler extends AbstractBpmnParseHandler<UserTask> {

    protected static final String MAGIC_WORD = "auto_evaluation";

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return UserTask.class;
    }

    @Override
    protected void executeParse(BpmnParse bpmnParse, UserTask task) {
        MultiInstanceLoopCharacteristics loopCharacteristics = task.getLoopCharacteristics();
        if (loopCharacteristics != null) {
            //A hack with magic word is necessary because if we define only sequential/parallel field but
            //inputDataItem and elementVariable fields are blank then activiti will not put
            //<multiInstanceLoopCharacteristics> to process XML at all. So the trick is to write a magic word in
            //"Collection (multi-instance)" field in modeler.
            if (MAGIC_WORD.equals(loopCharacteristics.getInputDataItem())) {
                //loopCharacteristics values defined in this block will not be overriden although
                //this handler is invoked before the default ones. This is because the loopCharacteristics have
                //been already parsed earlier (not in Activiti's UserTaskParseHandler)
                String procRole = getProcRole(task);
                loopCharacteristics.setInputDataItem("#{prm.getTaskAssigneeList(bpmProcInstanceId, '" + procRole + "')}");
                loopCharacteristics.setElementVariable("assignee");
                task.setAssignee("#{assignee}");
            }
        } else if (Strings.isNullOrEmpty(task.getAssignee())) {
            String procRole = getProcRole(task);
            if (!Strings.isNullOrEmpty(procRole)) {
                if ("true".equals(getClaimAllowed(task))) {
                    List<String> candidateUsers = Collections.singletonList("#{prm.getTaskAssigneeList(bpmProcInstanceId, '" + procRole + "')}");
                    task.setCandidateUsers(candidateUsers);
                } else {
                    task.setAssignee("#{prm.getSingleTaskAssignee(bpmProcInstanceId, '" + procRole + "')}");
                }
            }
        }
    }

    @Nullable
    protected String getProcRole(UserTask task) {
        Map<String, List<ExtensionElement>> extensionElements = task.getExtensionElements();
        List<ExtensionElement> procRoleElements = extensionElements.get("procRole");
        if (procRoleElements != null) {
            ExtensionElement procRoleElement = procRoleElements.get(0);
            return procRoleElement.getElementText();
        }

        return null;
    }

    protected String getClaimAllowed(UserTask task) {
        Map<String, List<ExtensionElement>> extensionElements = task.getExtensionElements();
        List<ExtensionElement> claimAllowedElements = extensionElements.get("claimAllowed");
        if (claimAllowedElements != null) {
            ExtensionElement claimAllowedElement = claimAllowedElements.get(0);
            return claimAllowedElement.getElementText();
        }

        return null;
    }
}
