/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.bpm.gui.action;

import com.haulmont.cuba.gui.components.actions.BaseAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Class provides an API for injecting some logic before and after
 * an action is performed
 * @author gorbunkov
 * @version $Id$
 */
public abstract class ProcAction extends BaseAction {

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
}
