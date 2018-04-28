/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.cuba.gui.components.AbstractAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Class provides an API for injecting some logic before and after
 * an action is performed
 */
public abstract class ProcAction extends AbstractAction {

    protected Supplier<Map<String, Object>> processVariablesSupplier;
    protected Supplier<Map<String, Object>> screenParametersSupplier;

    public interface BeforeActionPredicate {
        boolean evaluate();
    }

    public interface AfterActionListener {
        void actionCompleted();
    }

    protected List<BeforeActionPredicate> beforePredicates = new ArrayList<>();
    protected List<AfterActionListener> afterListeners = new ArrayList<>();

    protected ProcAction(String id) {
        super(id);
    }

    public void addBeforeActionPredicate(BeforeActionPredicate predicate) {
        if (predicate == null) return;
        beforePredicates.add(predicate);
    }

    public void addAfterActionListener(AfterActionListener listener) {
        if (listener == null) return;
        afterListeners.add(listener);
    }

    protected boolean evaluateBeforeActionPredicates() {
        for (BeforeActionPredicate predicate : beforePredicates) {
            if (!predicate.evaluate()) return false;
        }
        return true;
    }

    protected void fireAfterActionListeners() {
        for (AfterActionListener listener : afterListeners) {
            listener.actionCompleted();
        }
    }

    public Supplier<Map<String, Object>> getProcessVariablesSupplier() {
        return processVariablesSupplier;
    }

    public void setProcessVariablesSupplier(Supplier<Map<String, Object>> processVariablesSupplier) {
        this.processVariablesSupplier = processVariablesSupplier;
    }

    public Supplier<Map<String, Object>> getScreenParametersSupplier() {
        return screenParametersSupplier;
    }

    public void setScreenParametersSupplier(Supplier<Map<String, Object>> screenParametersSupplier) {
        this.screenParametersSupplier = screenParametersSupplier;
    }
}
