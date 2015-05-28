/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 *   Haulmont Technology proprietary and confidential.
 *   Use is subject to license terms.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.actions.BaseAction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gorbunkov
 * @version $Id$
 */
public abstract class ProcTaskAction extends BaseAction {

    public interface BeforeActionPredicate {
        boolean evaluate();
    }

    public interface AfterActionListener {
        void actionCompleted();
    }

    protected List<BeforeActionPredicate> beforePredicates = new ArrayList<>();
    protected List<AfterActionListener> afterListeners = new ArrayList<>();

    protected ProcTaskAction(String id) {
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
}
