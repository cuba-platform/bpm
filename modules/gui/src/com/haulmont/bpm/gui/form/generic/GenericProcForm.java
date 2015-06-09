/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.gui.form.generic;

import com.google.common.base.Strings;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcInstance;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.form.ProcFormParam;
import com.haulmont.bpm.gui.form.AbstractProcForm;
import com.haulmont.bpm.gui.form.generic.fieldgenerator.FormFieldGenerator;
import com.haulmont.bpm.gui.form.generic.fieldgenerator.FormFieldGeneratorsFactory;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.GridLayout;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Form that dynamically generates components for editing parameters that are defined
 * in form description in BPMN process xml.
 * @author gorbunkov
 * @version $Id$
 */
public class GenericProcForm extends AbstractProcForm {

    @WindowParam(name = "formDefinition", required = true)
    protected ProcFormDefinition formDefinition;

    @WindowParam(name = "procInstance", required = true)
    protected ProcInstance procInstance;

    @WindowParam(name = "procTask")
    protected ProcTask procTask;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected DataManager dataManager;

    protected Map<ProcFormParam, Field> componentsMap = new HashMap<>();

    protected static final String FIELD_WIDTH = "300px";

    @Inject
    protected ProcessMessagesService processMessagesService;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        if (!Strings.isNullOrEmpty(formDefinition.getCaption())) {
            String locCaption = processMessagesService.loadString(formDefinition.getActProcessDefinitionId(), formDefinition.getCaption());
            setCaption(locCaption);
        }
        createLayout();
    }

    protected void createLayout() {
        reloadProcInstance();
        if (procTask != null) {
            reloadProcTask();
        }
        String actExecutionId = procTask == null ? null : procTask.getActExecutionId();

        FormFieldGeneratorsFactory fieldGeneratorsFactory = new FormFieldGeneratorsFactory();
        List<ProcFormParam> formParams = formDefinition.getParams();
        GridLayout grid = componentsFactory.createComponent(GridLayout.class);
        grid.setRows(formParams.size());
        grid.setColumns(2);
        grid.setSpacing(true);
        int i = 0;
        for (ProcFormParam formParam : formParams) {

            Label label = componentsFactory.createComponent(Label.class);
            label.setValue(formParam.getLocCaption());
            label.setAlignment(Alignment.MIDDLE_LEFT);
            grid.add(label, 0, i);

            String paramTypeName = !Strings.isNullOrEmpty(formParam.getTypeName()) ? formParam.getTypeName() : "string";
            FormFieldGenerator generator = fieldGeneratorsFactory.createFormFieldGenerator(paramTypeName);
            Field field = generator.createField(formParam, actExecutionId);
            field.setWidth(FIELD_WIDTH);
            grid.add(field, 1, i);

            componentsMap.put(formParam, field);
            i++;
        }
        add(grid, 0);
    }

    protected void reloadProcInstance() {
        View view = new View(ProcInstance.class)
                .addProperty("procDefinition", new View(ProcDefinition.class).addProperty("actProcessInstanceId"));
        procInstance = dataManager.reload(procInstance, view);
    }

    protected void reloadProcTask() {
        View view = new View(ProcTask.class)
                .addProperty("actExecutionId");
        procTask = dataManager.reload(procTask, view);
    }

    @Override
    public Map<String, Object> getFormResult() {
        HashMap<String, Object> result = new HashMap<>();
        for (Map.Entry<ProcFormParam, Field> entry : componentsMap.entrySet()) {
            ProcFormParam formParam = entry.getKey();
            if (!formParam.isEditable()) continue;
            Field component = entry.getValue();
            result.put(formParam.getName(), component.getValue());
        }
        return result;
    }

    public void commit() {
        if (validateAll()) {
            close(COMMIT_ACTION_ID);
        }
    }

    public void cancel() {
        close(CLOSE_ACTION_ID);
    }
}
