/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.gui.proctaskactions;

import com.haulmont.bpm.form.ProcFormDefinition;
import com.haulmont.bpm.entity.ProcTask;
import com.haulmont.bpm.gui.form.ProcForm;
import com.haulmont.bpm.service.ProcessFormService;
import com.haulmont.bpm.service.ProcessMessagesService;
import com.haulmont.bpm.service.ProcessRuntimeService;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ProcTaskActionsFrame extends AbstractFrame {

    protected BoxLayout actionsBox;

    @Inject
    protected ProcessFormService processFormService;

    @Inject
    protected ProcessMessagesService processMessagesService;

    @Inject
    protected ProcessRuntimeService processRuntimeService;

    @Inject
    protected ComponentsFactory componentsFactory;

    protected List<ActionListener> actionListeners = new ArrayList<>();

    protected Orientation orientation = Orientation.HORIZONTAL;

    public interface ActionListener {
        void actionPerformed();
    }

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    public void addProcTaskActions(final ProcTask procTask) {
        if (actionsBox == null) {
            createActionsBox();
        }

        if (procTask.getProcActor() != null) {
            Map<String, ProcFormDefinition> outcomesWithForms = processFormService.getOutcomesWithForms(procTask);
            if (outcomesWithForms.isEmpty()) {
                //todo gorbunkov
            } else {
                for (Map.Entry<String, ProcFormDefinition> entry : outcomesWithForms.entrySet()) {
                    final String outcome = entry.getKey();
                    final ProcFormDefinition formDefinition = entry.getValue();
                    Button procActionBtn = componentsFactory.createComponent(Button.class);
                    procActionBtn.setAction(new ProcTaskAction(procTask, outcome, formDefinition));
                    actionsBox.add(procActionBtn);
                }
            }
        } else {
            Button claimTaskBtn = componentsFactory.createComponent(Button.class);
            claimTaskBtn.setCaption(getMessage("claimTask"));
            claimTaskBtn.setAction(new BaseAction("claimTask") {
                @Override
                public void actionPerform(Component component) {
                    processRuntimeService.claimProcTask(procTask, userSession.getCurrentOrSubstitutedUser());
                    fireActionListeners();
                }
            });
            actionsBox.add(claimTaskBtn);
        }
    }

    public void addProcessAction(AbstractAction action) {
        if (actionsBox == null)
            createActionsBox();

        Button button = componentsFactory.createComponent(Button.class);
        button.setAction(action);
        actionsBox.add(button);
    }

    protected void createActionsBox() {
        actionsBox = componentsFactory.createComponent(orientation == Orientation.HORIZONTAL
                ? HBoxLayout.class : VBoxLayout.class);
        actionsBox.setSpacing(true);
        add(actionsBox);
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    protected void fireActionListeners() {
        for (ActionListener listener : actionListeners) {
            listener.actionPerformed();
        }
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public void removeAllActions() {
        if (actionsBox != null)
            actionsBox.removeAll();
    }

    protected class ProcTaskAction extends BaseAction {

        private ProcTask procTask;
        private String outcome;
        private ProcFormDefinition formDefinition;

        protected ProcTaskAction(ProcTask procTask, String outcome, ProcFormDefinition formDefinition) {
            super(outcome);
            this.procTask = procTask;
            this.outcome = outcome;
            this.formDefinition = formDefinition;
        }

        @Override
        public void actionPerform(Component component) {
            if (formDefinition != null) {
                Map<String, Object> formParams = new HashMap<>();
                formParams.put("formDefinition", formDefinition);
                formParams.put("procTask", procTask);
                formParams.put("procInstance", procTask.getProcInstance());

                final Window procForm = openWindow(formDefinition.getName(), WindowManager.OpenType.DIALOG, formParams);
                procForm.addListener(new Window.CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                            String comment = null;
                            Map<String, Object> formResult = null;
                            if (procForm instanceof ProcForm) {
                                comment = ((ProcForm) procForm).getComment();
                                formResult = ((ProcForm) procForm).getFormResult();
                            }
                            processRuntimeService.completeProcTask(procTask, outcome, comment, formResult);
                            fireActionListeners();
                        }
                    }
                });
            } else {
                processRuntimeService.completeProcTask(procTask, outcome, null, new HashMap<String, Object>());
                fireActionListeners();
            }

        }

        @Override
        public String getCaption() {
            String key = procTask.getName() + "." + outcome;
            String message = processMessagesService.getMessage(procTask.getProcInstance().getProcDefinition().getActId(), key);
            if (message.equals(key)) {
                message = outcome;
            }
            return message;
        }
    }
}
